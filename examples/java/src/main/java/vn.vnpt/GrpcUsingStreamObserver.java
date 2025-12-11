package vn.vnpt;

public class GrpcUsingStreamObserver {
	protected abstract static class MakeRequestAudio<T> {
		abstract T makeMessage(com.google.protobuf.ByteString byteArray);

		protected void makeRequestAudio(io.grpc.stub.StreamObserver<T> requestObserver, String filePath, int chuckFile) {
			try (java.io.FileInputStream fis = new java.io.FileInputStream(filePath)) {
				byte[] buffer = new byte[chuckFile];
				while (true) {
					int n = fis.read(buffer);
					if (n <= 0) {
						break;
					}
					T request = makeMessage(com.google.protobuf.ByteString.copyFrom(buffer, 0, n));
					requestObserver.onNext(request);
				}
				requestObserver.onCompleted();
			} catch (Exception e) {
				e.printStackTrace();
				requestObserver.onError(e);
			}
		}
	}

	public static void main(String[] args) throws InterruptedException {
		final String AUTHORIZATION = "{{access_token}}";
		final String TOKEN_ID = "{{token_id}}";
		final String TOKEN_KEY = "{{token_key}}";
		final String HOST = "{{host}}";

		io.grpc.ManagedChannel channel = io.grpc.netty.NettyChannelBuilder.forTarget(HOST)
				.useTransportSecurity()
				.build();
		io.grpc.Metadata header = new io.grpc.Metadata();
		header.put(io.grpc.Metadata.Key.of("authorization", io.grpc.Metadata.ASCII_STRING_MARSHALLER), AUTHORIZATION);
		header.put(io.grpc.Metadata.Key.of("token-id", io.grpc.Metadata.ASCII_STRING_MARSHALLER), TOKEN_ID);
		header.put(io.grpc.Metadata.Key.of("token-key", io.grpc.Metadata.ASCII_STRING_MARSHALLER), TOKEN_KEY);

		vnpt.audio.asr.VnptSpeechRecognitionGrpc.VnptSpeechRecognitionStub stub = vnpt.audio.asr.VnptSpeechRecognitionGrpc
				.newStub(channel)
				.withInterceptors(io.grpc.stub.MetadataUtils.newAttachHeadersInterceptor(header));

		io.grpc.stub.StreamObserver<vnpt.audio.asr.VnptAsr.StreamingRecognizeRequest> streamObserver = stub.streamingRecognize(
				new io.grpc.stub.StreamObserver<>() {
					@Override
					public void onNext(vnpt.audio.asr.VnptAsr.StreamingRecognizeResponse message) {
						try {
							System.out.println(com.google.protobuf.util.JsonFormat.printer().alwaysPrintFieldsWithNoPresence().print(message));
						} catch (com.google.protobuf.InvalidProtocolBufferException e) {
							throw new RuntimeException(e);
						}
					}

					@Override
					public void onError(Throwable throwable) {
						throwable.printStackTrace();
					}

					@Override
					public void onCompleted() {
						System.out.println("onCompleted");
					}

				});


		vnpt.audio.asr.VnptAsr.StreamingRecognizeRequest config = vnpt.audio.asr.VnptAsr.StreamingRecognizeRequest.newBuilder()
				.setStreamingConfig(
						vnpt.audio.asr.VnptAsr.StreamingRecognitionConfig.newBuilder()
								.setConfig(vnpt.audio.asr.VnptAsr.RecognitionConfig.newBuilder()
										.setEncoding(vnpt.audio.VnptAudio.AudioEncoding.LINEAR_PCM)
										.setSampleRateHertz(8000) //thay thế
										.setLanguageCode("vi-VN")
										.setMaxAlternatives(1)
										.setAudioChannelCount(1)
										.setEnableWordTimeOffsets(false)
										.setEnableAutomaticPunctuation(false)
										.setEnableSeparateRecognitionPerChannel(false)
										.putAllCustomConfiguration(java.util.Map.of("invert_text", "1"))
										.setModel("fast_streaming").build())
								.setInterimResults(true)
								.build())
				.build();
		streamObserver.onNext(config);

		MakeRequestAudio<vnpt.audio.asr.VnptAsr.StreamingRecognizeRequest> make = new MakeRequestAudio<>() {
			@Override
			vnpt.audio.asr.VnptAsr.StreamingRecognizeRequest makeMessage(com.google.protobuf.ByteString byteArray) {
				return vnpt.audio.asr.VnptAsr.StreamingRecognizeRequest.newBuilder()
						.setAudioContent(byteArray)
						.build();
			}
		};

		make.makeRequestAudio(streamObserver, "file.wav", 1024);
		Thread.sleep(20000);
	}

}