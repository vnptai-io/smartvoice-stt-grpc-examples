

Hướng dẫn sử dụng API STT grpc với node.js



### 1. cài protoc và compile file proto

có nhiều cách cài và compile, trong đó có thể sử dụng protoc trong grpc-tools
```
npm install grpc-tools

mkdir compiled

./node_modules/.bin/grpc_tools_node_protoc --proto_path=./protos \
  --js_out=import_style=commonjs,binary:compiled \
  --grpc_out=grpc_js:compiled \
  protos/*.proto
```

### 2. cài các package grpc:

`npm install @grpc/grpc-js google-protobuf`

cài thêm thư viện đọc file wav vì ta sẽ stream data từ một file wav:

`npm install wav`


### 4. code client và chạy code

`node run_streaming.js`


hãy lưu ý set đúng các biến sau để chạy được
```
const audioFile = '../audio/4s.wav';
const serverAddress = '';
const metadata = [
  ['authorization', ''],
  ['token-id', ''],
  ['token-key', ''],
]
```



Notes:

với các ngôn ngữ khác ta cũng thực hiện các bước tương tự, chi tiết tùy thuộc vào từng ngôn ngữ

