package vn.vnpt;

import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.stub.StreamObserver;
import vnpt.audio.VnptAudio;
import vnpt.audio.asr.VnptAsr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Utility class chứa các hàm và constants dùng chung cho VNPT SmartVoice gRPC clients.
 *
 * <p>Class này cung cấp:
 * <ul>
 *   <li>Constants và cấu hình mặc định</li>
 *   <li>Helper methods để tạo metadata và config</li>
 *   <li>Abstract handlers để stream audio</li>
 * </ul>
 *
 * @author VNPT
 * @version 1.0
 * @since 2025
 */
public final class SmartVoiceClientUtils {

    // ==================== CONSTANTS ====================

    /** Default chunk size cho việc đọc file audio (2KB) */
    public static final int DEFAULT_CHUNK_SIZE = 2048;

    /** Timeout mặc định để chờ response (30 giây) */
    public static final long DEFAULT_TIMEOUT_SECONDS = 30;

    /** Sample rate cho audio LINEAR_PCM 8kHz */
    public static final int SAMPLE_RATE_HZ = 8000;

    /** Số lượng alternative transcriptions tối đa */
    public static final int MAX_ALTERNATIVES = 1;

    /** Số kênh audio */
    public static final int AUDIO_CHANNEL_COUNT = 1;

    /** Mã ngôn ngữ tiếng Việt */
    public static final String LANGUAGE_CODE = "vi-VN";

    /** Model nhận dạng giọng nói streaming nhanh */
    public static final String MODEL_NAME = "fast_streaming";

    /** Host mặc định của VNPT SmartVoice gRPC service */
    public static final String DEFAULT_HOST = "grpc.vnpt.vn";

    // Private constructor để prevent instantiation
    private SmartVoiceClientUtils() {
        throw new AssertionError("Utility class không thể được khởi tạo");
    }

    // ==================== METADATA HELPERS ====================

    /**
     * Tạo metadata headers cho authentication.
     *
     * @param authorization Access token (có thể bao gồm prefix "bearer")
     * @param tokenId Token ID
     * @param tokenKey Token key
     * @return Metadata chứa authentication headers
     * @throws IllegalArgumentException nếu bất kỳ tham số nào null hoặc empty
     */
    public static Metadata createAuthMetadata(String authorization, String tokenId, String tokenKey) {
        validateNotEmpty(authorization, "authorization");
        validateNotEmpty(tokenId, "tokenId");
        validateNotEmpty(tokenKey, "tokenKey");

        Metadata metadata = new Metadata();
        metadata.put(
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER),
            authorization
        );
        metadata.put(
            Metadata.Key.of("token-id", Metadata.ASCII_STRING_MARSHALLER),
            tokenId
        );
        metadata.put(
            Metadata.Key.of("token-key", Metadata.ASCII_STRING_MARSHALLER),
            tokenKey
        );
        return metadata;
    }

    // ==================== CONFIG BUILDERS ====================

    /**
     * Tạo cấu hình nhận dạng giọng nói với các tham số mặc định.
     *
     * @return RecognitionConfig với cấu hình mặc định
     */
    public static VnptAsr.RecognitionConfig createDefaultRecognitionConfig() {
        return createRecognitionConfig(
            VnptAudio.AudioEncoding.LINEAR_PCM,
            SAMPLE_RATE_HZ,
            LANGUAGE_CODE,
            MODEL_NAME,
            false,  // enableWordTimeOffsets
            false   // enableAutomaticPunctuation
        );
    }

    /**
     * Tạo cấu hình nhận dạng giọng nói với các tham số tùy chỉnh.
     *
     * @param encoding Audio encoding format
     * @param sampleRateHertz Sample rate (Hz)
     * @param languageCode Mã ngôn ngữ (ví dụ: "vi-VN")
     * @param model Model nhận dạng
     * @param enableWordTimeOffsets Bật timestamp cho từng từ
     * @param enableAutomaticPunctuation Bật tự động thêm dấu câu
     * @return RecognitionConfig với cấu hình được chỉ định
     */
    public static VnptAsr.RecognitionConfig createRecognitionConfig(
            VnptAudio.AudioEncoding encoding,
            int sampleRateHertz,
            String languageCode,
            String model,
            boolean enableWordTimeOffsets,
            boolean enableAutomaticPunctuation) {

        return VnptAsr.RecognitionConfig.newBuilder()
                .setEncoding(encoding)
                .setSampleRateHertz(sampleRateHertz)
                .setLanguageCode(languageCode)
                .setMaxAlternatives(MAX_ALTERNATIVES)
                .setAudioChannelCount(AUDIO_CHANNEL_COUNT)
                .setEnableWordTimeOffsets(enableWordTimeOffsets)
                .setEnableAutomaticPunctuation(enableAutomaticPunctuation)
                .setEnableSeparateRecognitionPerChannel(false)
                .putAllCustomConfiguration(Map.of("invert_text", "1"))
                .setModel(model)
                .build();
    }

    /**
     * Tạo streaming config request.
     *
     * @param config RecognitionConfig
     * @param enableInterimResults Có trả về kết quả interim hay không
     * @return StreamingRecognizeRequest chứa config
     */
    public static VnptAsr.StreamingRecognizeRequest createConfigRequest(
            VnptAsr.RecognitionConfig config,
            boolean enableInterimResults) {

        return VnptAsr.StreamingRecognizeRequest.newBuilder()
                .setStreamingConfig(
                    VnptAsr.StreamingRecognitionConfig.newBuilder()
                        .setConfig(config)
                        .setInterimResults(enableInterimResults)
                        .build()
                )
                .build();
    }

    /**
     * Tạo streaming config request với cấu hình mặc định.
     *
     * @param enableInterimResults Có trả về kết quả interim hay không
     * @return StreamingRecognizeRequest chứa config mặc định
     */
    public static VnptAsr.StreamingRecognizeRequest createDefaultConfigRequest(boolean enableInterimResults) {
        return createConfigRequest(createDefaultRecognitionConfig(), enableInterimResults);
    }

    // ==================== VALIDATION HELPERS ====================

    /**
     * Validate string không null và không empty.
     *
     * @param value Giá trị cần validate
     * @param paramName Tên parameter (để hiển thị trong error message)
     * @throws IllegalArgumentException nếu value null hoặc empty
     */
    private static void validateNotEmpty(String value, String paramName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(paramName + " không được null hoặc empty");
        }
    }

    /**
     * Validate file path và kiểm tra file tồn tại.
     *
     * @param filePath Đường dẫn file
     * @return File object nếu hợp lệ
     * @throws IllegalArgumentException nếu file path không hợp lệ hoặc file không tồn tại
     */
    public static File validateAudioFile(String filePath) {
        validateNotEmpty(filePath, "filePath");

        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("File không tồn tại: " + filePath);
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("Path không phải là file: " + filePath);
        }
        if (!file.canRead()) {
            throw new IllegalArgumentException("Không thể đọc file: " + filePath);
        }

        return file;
    }

    // ==================== AUDIO STREAM HANDLERS ====================

    /**
     * Abstract class để xử lý việc stream audio data sử dụng ClientCall.
     *
     * @param <T> Kiểu message request
     */
    public abstract static class ClientCallAudioStreamHandler<T> {

        private final ClientCall<T, ?> clientCall;

        /**
         * Khởi tạo handler với ClientCall.
         *
         * @param clientCall ClientCall để gửi audio data
         * @throws IllegalArgumentException nếu clientCall null
         */
        public ClientCallAudioStreamHandler(ClientCall<T, ?> clientCall) {
            if (clientCall == null) {
                throw new IllegalArgumentException("ClientCall không được null");
            }
            this.clientCall = clientCall;
        }

        /**
         * Tạo message request từ audio data.
         *
         * @param audioData Audio data dạng ByteString
         * @return Message request chứa audio data
         */
        protected abstract T makeMessage(com.google.protobuf.ByteString audioData);

        /**
         * Đọc file audio và stream lên server theo chunks.
         *
         * @param filePath Đường dẫn tới file audio
         * @param chunkSize Kích thước mỗi chunk (bytes)
         */
        public void streamAudioFile(String filePath, int chunkSize) {
            if (chunkSize <= 0) {
                throw new IllegalArgumentException("Chunk size phải lớn hơn 0");
            }

            File audioFile = validateAudioFile(filePath);

            try (FileInputStream fis = new FileInputStream(audioFile)) {
                byte[] buffer = new byte[chunkSize];
                int bytesRead;

                while ((bytesRead = fis.read(buffer)) > 0) {
                    T request = makeMessage(com.google.protobuf.ByteString.copyFrom(buffer, 0, bytesRead));
                    clientCall.sendMessage(request);
                    clientCall.request(1);
                }

                clientCall.halfClose();
                System.out.println("Đã gửi xong file audio: " + filePath);

            } catch (IOException e) {
                String errorMsg = "Lỗi khi đọc file audio: " + e.getMessage();
                System.err.println(errorMsg);
                e.printStackTrace();
                clientCall.cancel(errorMsg, e);
            } catch (Exception e) {
                String errorMsg = "Lỗi không mong muốn: " + e.getMessage();
                System.err.println(errorMsg);
                e.printStackTrace();
                clientCall.cancel(errorMsg, e);
            }
        }
    }

    /**
     * Abstract class để xử lý việc stream audio data sử dụng StreamObserver.
     *
     * @param <T> Kiểu message request
     */
    public abstract static class StreamObserverAudioStreamHandler<T> {

        /**
         * Tạo message request từ audio data.
         *
         * @param audioData Audio data dạng ByteString
         * @return Message request chứa audio data
         */
        protected abstract T makeMessage(com.google.protobuf.ByteString audioData);

        /**
         * Đọc file audio và stream lên server theo chunks.
         *
         * @param requestObserver StreamObserver để gửi audio data
         * @param filePath Đường dẫn tới file audio
         * @param chunkSize Kích thước mỗi chunk (bytes)
         */
        public void streamAudioFile(StreamObserver<T> requestObserver, String filePath, int chunkSize) {
            if (requestObserver == null) {
                throw new IllegalArgumentException("StreamObserver không được null");
            }
            if (chunkSize <= 0) {
                throw new IllegalArgumentException("Chunk size phải lớn hơn 0");
            }

            File audioFile;
            try {
                audioFile = validateAudioFile(filePath);
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
                requestObserver.onError(new IOException(e.getMessage()));
                return;
            }

            try (FileInputStream fis = new FileInputStream(audioFile)) {
                byte[] buffer = new byte[chunkSize];
                int bytesRead;

                while ((bytesRead = fis.read(buffer)) > 0) {
                    T request = makeMessage(com.google.protobuf.ByteString.copyFrom(buffer, 0, bytesRead));
                    requestObserver.onNext(request);
                }

                requestObserver.onCompleted();
                System.out.println("Đã gửi xong file audio: " + filePath);

            } catch (IOException e) {
                String errorMsg = "Lỗi khi đọc file audio: " + e.getMessage();
                System.err.println(errorMsg);
                e.printStackTrace();
                requestObserver.onError(e);
            } catch (Exception e) {
                String errorMsg = "Lỗi không mong muốn: " + e.getMessage();
                System.err.println(errorMsg);
                e.printStackTrace();
                requestObserver.onError(e);
            }
        }
    }

    // ==================== RESPONSE HELPERS ====================

    /**
     * In response dạng JSON ra console.
     *
     * @param message Response message
     */
    public static void printResponse(VnptAsr.StreamingRecognizeResponse message) {
        try {
            String jsonMessage = com.google.protobuf.util.JsonFormat.printer()
                    .alwaysPrintFieldsWithNoPresence()
                    .print(message);
            System.out.println("=== Nhận được kết quả ===");
            System.out.println(jsonMessage);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
            System.err.println("Lỗi khi parse message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
