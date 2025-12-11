# VNPT SmartVoice gRPC Client - Java Examples

Tài liệu hướng dẫn sử dụng Java gRPC client để kết nối với dịch vụ nhận dạng giọng nói (ASR - Automatic Speech Recognition) của VNPT SmartVoice.

## Tổng quan

Project này cung cấp 2 ví dụ triển khai gRPC client để kết nối với VNPT SmartVoice ASR Service:

1. **GrpcUsingClientCall** - Sử dụng ClientCall API (Low-level)
   - Kiểm soát chi tiết về flow control và back-pressure
   - Phù hợp cho các use case phức tạp cần kiểm soát tối đa

2. **GrpcUsingStreamObserver** - Sử dụng StreamObserver API (High-level)
   - Đơn giản, dễ sử dụng
   - Được khuyến nghị cho hầu hết các use case thông thường

3. **SmartVoiceClientUtils** - Utility class dùng chung
   - Chứa constants và configuration mặc định
   - Helper methods để tạo metadata, config
   - Abstract handlers để stream audio
   - Giảm code duplication và tăng tính maintainability

## Yêu cầu hệ thống

- **Java**: JDK 21 hoặc cao hơn
- **Maven**: 3.6.0 hoặc cao hơn
- **Audio file**: File WAV format (LINEAR_PCM, 8kHz, mono)

## Cài đặt

### 1. Clone repository

```bash
git clone https://github.com/vnptai-io/smartvoice-stt-grpc-examples.git
cd smartvoice-stt-grpc-examples/examples/java
```

### 2. Build project

```bash
mvn clean install
```

Lệnh này sẽ:
- Download các dependencies
- Generate Java code từ Protocol Buffer files (.proto)
- Compile project

### 3. Verify build

```bash
mvn compile
```

Kiểm tra các generated classes trong thư mục `target/generated-sources/protobuf/`:
- `vnpt.audio.asr.VnptAsr` - ASR service messages
- `vnpt.audio.asr.VnptSpeechRecognitionGrpc` - gRPC service stubs
- `vnpt.audio.VnptAudio` - Audio encoding definitions

## Cấu trúc dự án

```
examples/java/
├── pom.xml                          # Maven configuration
├── README.md                        # Tài liệu này
├── file.wav                         # Sample audio file
├── src/
│   └── main/
│       ├── java/
│       │   └── vn.vnpt/
│       │       ├── GrpcUsingClientCall.java       # ClientCall API example
│       │       ├── GrpcUsingStreamObserver.java   # StreamObserver API example
│       │       └── SmartVoiceClientUtils.java     # Shared utilities & helpers
│       └── proto/
│           ├── vnpt_asr.proto       # ASR service definitions
│           └── vnpt_audio.proto     # Audio encoding definitions
└── target/
    └── generated-sources/           # Generated gRPC code
```

## Cấu hình

### 1. Lấy credentials

Để sử dụng VNPT SmartVoice API, bạn cần có:
- `access_token` - Token xác thực (có thể bao gồm prefix "bearer")
- `token_id` - ID của token
- `token_key` - Key của token

Liên hệ VNPT để được cấp credentials.

### 2. Cập nhật credentials trong code

Mở file Java bạn muốn chạy và thay thế các placeholder:

**GrpcUsingClientCall.java** hoặc **GrpcUsingStreamObserver.java**:

```java
// Thay thế các giá trị này
final String AUTHORIZATION = "bearer {{your_access_token}}";  // Thay bằng access token thực
final String TOKEN_ID = "{{your_token_id}}";                  // Thay bằng token ID thực
final String TOKEN_KEY = "{{your_token_key}}";                // Thay bằng token key thực
```

**Lưu ý:** Không commit credentials vào git. Nên sử dụng environment variables hoặc config files.

### 3. Chuẩn bị file audio

Đặt file audio của bạn vào thư mục `examples/java/` hoặc cập nhật đường dẫn:

```java
final String AUDIO_FILE_PATH = "file.wav";  // Hoặc đường dẫn tuyệt đối
```

**File audio mẫu:**
- Format: WAV (LINEAR_PCM)
- Sample rate: 8000 Hz
- Channels: 1 (mono)
- Bit depth: 16-bit

File audio mẫu và file của bạn có thể khác nhau về thông số cấu hình, hãy detect file audio của bạn để cấu hình đúng để đảm bảo chất lượng đầu ra

## Sử dụng

### Chạy với Maven

#### Sử dụng StreamObserver API (Khuyến nghị)

```bash
mvn exec:java -Dexec.mainClass="vn.vnpt.GrpcUsingStreamObserver"
```

#### Sử dụng ClientCall API

```bash
mvn exec:java -Dexec.mainClass="vn.vnpt.GrpcUsingClientCall"
```

### Chạy với compiled JAR

```bash
# Build JAR file
mvn package

# Chạy
java -cp target/sample.grpc-1.0-SNAPSHOT.jar vn.vnpt.GrpcUsingStreamObserver
```

### Chạy từ IDE

1. Import project vào IDE (IntelliJ IDEA, Eclipse, VS Code)
2. Chọn main class:
   - `vn.vnpt.GrpcUsingClientCall`
   - `vn.vnpt.GrpcUsingStreamObserver`
3. Run
