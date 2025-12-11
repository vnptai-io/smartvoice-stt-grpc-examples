
Các dịch vụ gRPC và messages/data structures được định nghĩa bằng các file proto. Các file này được dùng để generate bindings tích hợp vào ứng dụng của bạn. 

Quá trình tích hợp dịch vụ gRPC chi tiết phụ thuộc vào từng ngôn ngữ, nhưng tổng quan các bước đều giống nhau:

1. Cài đặt thư viện gRPC cho ngôn ngữ mục tiêu của bạn.
2. Generate bindings bằng protoc.
3. Viết code client theo theo ngôn ngữ bạn muốn và sử dụng bindings  

(tham khảo thêm tài liệu gRPC: https://grpc.io/docs/)



Hiện API STT gRPC đã có ví dụ tích hợp cụ thể cho python, java, js

với các bindings đã compiled sẵn từ file proto, có thể lấy dùng trực tiếp 
