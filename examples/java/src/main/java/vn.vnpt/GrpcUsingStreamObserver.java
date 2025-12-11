package vn.vnpt;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import vnpt.audio.asr.VnptAsr;
import vnpt.audio.asr.VnptSpeechRecognitionGrpc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static vn.vnpt.SmartVoiceClientUtils.*;

/**
 * Example gRPC client sử dụng StreamObserver API để kết nối với VNPT SmartVoice ASR Service.
 *
 * <p>StreamObserver API là high-level API đơn giản và dễ sử dụng cho gRPC streaming.
 * Phương pháp này được khuyến nghị cho hầu hết các use cases thông thường.
 *
 * <p>Ví dụ này minh họa:
 * <ul>
 *   <li>Thiết lập kết nối gRPC với TLS</li>
 *   <li>Xác thực sử dụng metadata interceptor</li>
 *   <li>Streaming audio file theo chunks</li>
 *   <li>Xử lý kết quả nhận dạng giọng nói real-time</li>
 * </ul>
 *
 * <p><b>So với ClientCall API:</b>
 * <ul>
 *   <li>Đơn giản hơn, dễ sử dụng hơn</li>
 *   <li>Phù hợp cho hầu hết use cases</li>
 *   <li>Flow control được quản lý tự động</li>
 * </ul>
 *
 * @author VNPT
 * @version 1.0
 * @since 2025
 * @see GrpcUsingClientCall
 * @see SmartVoiceClientUtils
 */
public class GrpcUsingStreamObserver {

    /**
     * Response handler để xử lý kết quả nhận dạng từ server.
     *
     * <p>Implements StreamObserver để nhận và xử lý streaming responses từ VNPT ASR service.
     */
    private static class RecognitionResponseHandler implements StreamObserver<VnptAsr.StreamingRecognizeResponse> {

        private final CountDownLatch completionLatch;

        /**
         * Khởi tạo response handler.
         *
         * @param completionLatch CountDownLatch để signal khi hoàn thành
         */
        public RecognitionResponseHandler(CountDownLatch completionLatch) {
            this.completionLatch = completionLatch;
        }

        @Override
        public void onNext(VnptAsr.StreamingRecognizeResponse message) {
            printResponse(message);
        }

        @Override
        public void onError(Throwable throwable) {
            System.err.println("=== Lỗi từ server ===");
            System.err.println("Error: " + throwable.getMessage());
            throwable.printStackTrace();
            completionLatch.countDown();
        }

        @Override
        public void onCompleted() {
            System.out.println("=== Kết thúc stream ===");
            System.out.println("Server đã hoàn thành việc xử lý");
            completionLatch.countDown();
        }
    }

    /**
     * Main method minh họa cách sử dụng StreamObserver API để stream audio tới VNPT ASR service.
     *
     * <p>Các bước thực hiện:
     * <ol>
     *   <li>Tạo gRPC channel với TLS</li>
     *   <li>Tạo stub với authentication headers</li>
     *   <li>Tạo bidirectional streaming call</li>
     *   <li>Gửi config request</li>
     *   <li>Stream audio file theo chunks</li>
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

            // ========== BƯỚC 3: TẠO STUB VỚI INTERCEPTOR ==========
            VnptSpeechRecognitionGrpc.VnptSpeechRecognitionStub stub =
                VnptSpeechRecognitionGrpc.newStub(channel)
                    .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));

            // Sử dụng CountDownLatch để đợi response
            CountDownLatch completionLatch = new CountDownLatch(1);

            // ========== BƯỚC 4: TẠO BIDIRECTIONAL STREAMING CALL ==========
            RecognitionResponseHandler responseHandler = new RecognitionResponseHandler(completionLatch);
            StreamObserver<VnptAsr.StreamingRecognizeRequest> requestObserver =
                stub.streamingRecognize(responseHandler);

            // ========== BƯỚC 5: GỬI CONFIG REQUEST ==========
            VnptAsr.StreamingRecognizeRequest configRequest = createDefaultConfigRequest(true);
            requestObserver.onNext(configRequest);
            System.out.println("Đã gửi config request");

            // ========== BƯỚC 6: STREAM AUDIO FILE ==========
            StreamObserverAudioStreamHandler<VnptAsr.StreamingRecognizeRequest> audioStreamHandler =
                new StreamObserverAudioStreamHandler<>() {
                    @Override
                    protected VnptAsr.StreamingRecognizeRequest makeMessage(com.google.protobuf.ByteString audioData) {
                        return VnptAsr.StreamingRecognizeRequest.newBuilder()
                                .setAudioContent(audioData)
                                .build();
                    }
                };

            System.out.println("Đang stream audio file: " + AUDIO_FILE_PATH);
            audioStreamHandler.streamAudioFile(requestObserver, AUDIO_FILE_PATH, DEFAULT_CHUNK_SIZE);

            // ========== BƯỚC 7: ĐỢI HOÀN THÀNH ==========
            if (!completionLatch.await(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                System.err.println("Timeout sau " + DEFAULT_TIMEOUT_SECONDS + " giây");
                requestObserver.onError(new RuntimeException("Timeout waiting for response"));
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
