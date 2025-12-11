package vn.vnpt;

import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.netty.NettyChannelBuilder;
import vnpt.audio.asr.VnptAsr;
import vnpt.audio.asr.VnptSpeechRecognitionGrpc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static vn.vnpt.SmartVoiceClientUtils.*;

/**
 * Example gRPC client sử dụng ClientCall API để kết nối với VNPT SmartVoice ASR Service.
 *
 * <p>ClientCall API là low-level API cho phép kiểm soát chi tiết hơn về luồng gRPC streaming.
 * Phương pháp này được khuyến nghị khi cần kiểm soát flow control và back-pressure.
 *
 * <p>Ví dụ này minh họa:
 * <ul>
 *   <li>Thiết lập kết nối gRPC với TLS</li>
 *   <li>Xác thực sử dụng metadata headers</li>
 *   <li>Streaming audio file theo chunks</li>
 *   <li>Xử lý kết quả nhận dạng giọng nói real-time</li>
 * </ul>
 *
 * <p><b>So với StreamObserver API:</b>
 * <ul>
 *   <li>Kiểm soát chi tiết hơn về flow control</li>
 *   <li>Phù hợp cho streaming audio lớn</li>
 *   <li>Code phức tạp hơn một chút</li>
 * </ul>
 *
 * @author VNPT
 * @version 1.0
 * @since 2025
 * @see GrpcUsingStreamObserver
 * @see SmartVoiceClientUtils
 */
public class GrpcUsingClientCall {

    /**
     * Main method minh họa cách sử dụng ClientCall API để stream audio tới VNPT ASR service.
     *
     * <p>Các bước thực hiện:
     * <ol>
     *   <li>Tạo gRPC channel với TLS</li>
     *   <li>Tạo ClientCall với authentication headers</li>
     *   <li>Gửi config request</li>
     *   <li>Stream audio file theo chunks</li>
     *   <li>Nhận và xử lý kết quả nhận dạng</li>
     *   <li>Đợi cho đến khi hoàn thành</li>
     *   <li>Đóng channel</li>
     * </ol>
     *
     * @param args Command line arguments (không sử dụng)
     * @throws InterruptedException nếu thread bị interrupt trong khi chờ
     */
    public static void main(String[] args) throws InterruptedException {
        // ========== CẤU HÌNH ==========
        // TODO: Thay thế các giá trị placeholder bằng credentials thực tế
        final String AUTHORIZATION = "{{access_token}}";
        final String TOKEN_ID = "{{token_id}}";
        final String TOKEN_KEY = "{{token_key}}";
        final String AUDIO_FILE_PATH = "file.wav";

        ManagedChannel channel = null;

        try {
            // ========== BƯỚC 1: TẠO GRPC CHANNEL ==========
            System.out.println("Đang kết nối tới " + DEFAULT_HOST + "...");
            channel = NettyChannelBuilder.forTarget(DEFAULT_HOST)
                    .useTransportSecurity()
                    .build();

            // ========== BƯỚC 2: TẠO METADATA HEADERS ==========
            Metadata headers = createAuthMetadata(AUTHORIZATION, TOKEN_ID, TOKEN_KEY);

            // ========== BƯỚC 3: TẠO CLIENT CALL ==========
            ClientCall<VnptAsr.StreamingRecognizeRequest, VnptAsr.StreamingRecognizeResponse> clientCall =
                channel.newCall(
                    VnptSpeechRecognitionGrpc.getStreamingRecognizeMethod(),
                    io.grpc.CallOptions.DEFAULT
                );

            // Sử dụng CountDownLatch để đợi response
            CountDownLatch completionLatch = new CountDownLatch(1);

            // ========== BƯỚC 4: START CLIENT CALL VỚI LISTENER ==========
            clientCall.start(new ClientCall.Listener<VnptAsr.StreamingRecognizeResponse>() {
                @Override
                public void onHeaders(Metadata responseHeaders) {
                    System.out.println("=== Response Headers ===");
                    responseHeaders.keys().forEach(key -> {
                        String value = responseHeaders.get(
                            Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER)
                        );
                        System.out.println(key + ": " + value);
                    });
                }

                @Override
                public void onMessage(VnptAsr.StreamingRecognizeResponse message) {
                    printResponse(message);
                }

                @Override
                public void onClose(Status status, Metadata trailers) {
                    System.out.println("=== Kết thúc stream ===");
                    System.out.println("Status: " + status);
                    if (!status.isOk()) {
                        System.err.println("Lỗi: " + status.getDescription());
                    }
                    completionLatch.countDown();
                }
            }, headers);

            // ========== BƯỚC 5: GỬI CONFIG REQUEST ==========
            VnptAsr.StreamingRecognizeRequest configRequest = createDefaultConfigRequest(true);
            clientCall.sendMessage(configRequest);
            clientCall.request(1);
            System.out.println("Đã gửi config request");

            // ========== BƯỚC 6: STREAM AUDIO FILE ==========
            ClientCallAudioStreamHandler<VnptAsr.StreamingRecognizeRequest> audioStreamHandler =
                new ClientCallAudioStreamHandler<>(clientCall) {
                    @Override
                    protected VnptAsr.StreamingRecognizeRequest makeMessage(com.google.protobuf.ByteString audioData) {
                        return VnptAsr.StreamingRecognizeRequest.newBuilder()
                                .setAudioContent(audioData)
                                .build();
                    }
                };

            System.out.println("Đang stream audio file: " + AUDIO_FILE_PATH);
            audioStreamHandler.streamAudioFile(AUDIO_FILE_PATH, DEFAULT_CHUNK_SIZE);

            // ========== BƯỚC 7: ĐỢI HOÀN THÀNH ==========
            if (!completionLatch.await(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                System.err.println("Timeout sau " + DEFAULT_TIMEOUT_SECONDS + " giây");
                clientCall.cancel("Timeout", null);
            }

            System.out.println("Hoàn thành!");

        } finally {
            // ========== BƯỚC 8: ĐÓNG CHANNEL ==========
            if (channel != null) {
                System.out.println("Đang đóng kết nối...");
                channel.shutdown();
                try {
                    if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
                        channel.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    channel.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
