package vn.vnpt;

import io.grpc.ClientCall;

import java.util.concurrent.atomic.AtomicBoolean;

// Recommended
public class GrpcUsingClientCall {

	protected abstract static class MakeRequestAudio<T> {

		private final ClientCall<T, ?> clientCall;

		public MakeRequestAudio(ClientCall<T, ?> clientCall) {
			this.clientCall = clientCall;
		}

		abstract T makeMessage(com.google.protobuf.ByteString byteArray);

		protected void makeRequestAudio(String filePath, int chuckFile) {
			java.io.File f = new java.io.File(filePath);
			try (java.io.FileInputStream fis = new java.io.FileInputStream(f)) {
				byte[] buffer = new byte[chuckFile];
				while (true) {
					int n = fis.read(buffer);
					if (n <= 0) {
						break;
					}
					T request = makeMessage(com.google.protobuf.ByteString.copyFrom(buffer, 0, n));
					clientCall.sendMessage(request);
					clientCall.request(1);
				}
				clientCall.halfClose();
			} catch (Exception e) {
				e.printStackTrace();
				clientCall.cancel("unexpected error: " + e.getMessage(), e);
			}
		}
	}

	public static void main(String[] args) throws InterruptedException {
		final String AUTHORIZATION = "bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJkOTIwODRmMS03ODVlLTExZWUtOTRjZC1hZmU4MDRiZGUwNDgiLCJhdWQiOlsicmVzdHNlcnZpY2UiXSwidXNlcl9uYW1lIjoidHJhbnF1YW5ndHJ1b25nLmRldkBnbWFpbC5jb20iLCJzY29wZSI6WyJyZWFkIl0sImlzcyI6Imh0dHBzOi8vbG9jYWxob3N0IiwibmFtZSI6InRyYW5xdWFuZ3RydW9uZy5kZXZAZ21haWwuY29tIiwidXVpZF9hY2NvdW50IjoiZDkyMDg0ZjEtNzg1ZS0xMWVlLTk0Y2QtYWZlODA0YmRlMDQ4IiwiYXV0aG9yaXRpZXMiOlsiVVNFUiJdLCJqdGkiOiI5YWMwOWFlNi1hZTExLTQwMDktYTkxOS04YzNlMjZiMWEzNjciLCJjbGllbnRfaWQiOiJhZG1pbmFwcCJ9.0Qv515SxnWg5ISN-YID6qW775WvypszMkf1DB9-aEoDwfMi9BNCFS1LFrNj98PfdhvFWlvLEQyXp6RI50VYUYOxKl9iTKyfw7Lm7ZdtdBBn27P0lqi8n5upMhn-iLO-zcrM2Eg-YIDaOnDnDtPdJ9ZYdoDuhyIvViJoos2bH8d_k0mFuaYkxMMKq4g4jRXZchxBrU84reqpCA4b3uayziF99m8QBxvzTMU3xAbw6CHke6rb8wheyw_4h9jKIgahgHIXQ_EkPBAKiES0kdPA7blue20BouPUIfpA0Hq1Xi0ukECUQK6gxWzU3jSqjqbutjrpJtqkMfYq4PzdBRJJrGg";
		final String TOKEN_ID = "090f29df-e589-4b74-e063-62199f0a5905";
		final String TOKEN_KEY = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJ6NcdJ31NQj1BXcBJfXFtsVT2VJc6gjmKjV57D8fUYMvQErYD9E85N0xBO0ZWlVE9XhJe6NQ7fI3lQJxwRKHSMCAwEAAQ==";
		final String HOST = "grpc.vnpt.vn";

		io.grpc.ManagedChannel channel = io.grpc.netty.NettyChannelBuilder.forTarget(HOST)
				.useTransportSecurity()
				.build();

		io.grpc.Metadata header = new io.grpc.Metadata();
		header.put(io.grpc.Metadata.Key.of("authorization", io.grpc.Metadata.ASCII_STRING_MARSHALLER), AUTHORIZATION);
		header.put(io.grpc.Metadata.Key.of("token-id", io.grpc.Metadata.ASCII_STRING_MARSHALLER), TOKEN_ID);
		header.put(io.grpc.Metadata.Key.of("token-key", io.grpc.Metadata.ASCII_STRING_MARSHALLER), TOKEN_KEY);
		header.put(io.grpc.Metadata.Key.of("bot-id", io.grpc.Metadata.ASCII_STRING_MARSHALLER), "truong-bot-id");
		header.put(io.grpc.Metadata.Key.of("service-code", io.grpc.Metadata.ASCII_STRING_MARSHALLER), "emotion-service");

		io.grpc.ClientCall<vnpt.audio.asr.VnptAsr.StreamingRecognizeRequest, vnpt.audio.asr.VnptAsr.StreamingRecognizeResponse> clientCall = channel
				.newCall(vnpt.audio.asr.VnptSpeechRecognitionGrpc.getStreamingRecognizeMethod(), io.grpc.CallOptions.DEFAULT);
		AtomicBoolean stop = new AtomicBoolean(false);
		clientCall.start(new io.grpc.ClientCall.Listener<>() {
			                 @Override
			                 public void onHeaders(io.grpc.Metadata headers) {
				                 headers.keys().forEach(k -> System.out.println("KEY: " + k + ", VALUE: " + headers.get(io.grpc.Metadata.Key.of(k, io.grpc.Metadata.ASCII_STRING_MARSHALLER))));
			                 }

			                 @Override
			                 public void onMessage(vnpt.audio.asr.VnptAsr.StreamingRecognizeResponse message) {
				                 try {
					                 String messageStr = com.google.protobuf.util.JsonFormat.printer().print(message);
					                 System.out.println("onMessage" + messageStr);
				                 } catch (com.google.protobuf.InvalidProtocolBufferException e) {
					                 throw new RuntimeException(e);
				                 }
			                 }

			                 @Override
			                 public void onClose(io.grpc.Status status, io.grpc.Metadata trailers) {
				                 System.out.println("onClose" + status);
				                 stop.set(true);
			                 }
		                 }, header
		);

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
		clientCall.sendMessage(config);
		clientCall.request(1);


		MakeRequestAudio<vnpt.audio.asr.VnptAsr.StreamingRecognizeRequest> make = new MakeRequestAudio<>(clientCall) {
			@Override
			vnpt.audio.asr.VnptAsr.StreamingRecognizeRequest makeMessage(com.google.protobuf.ByteString byteArray) {
				return vnpt.audio.asr.VnptAsr.StreamingRecognizeRequest.newBuilder()
						.setAudioContent(byteArray)
						.build();
			}
		};

		make.makeRequestAudio("file.wav", 2048);
		while (!stop.get()) {
			Thread.sleep(100);
		}
	}
}
