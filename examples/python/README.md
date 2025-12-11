
Hướng dẫn sử dụng API STT grpc với python


### 1. cài protoc và compile file proto

tạo folder protos_compiled và file __init__.py
```
mkdir protos_compiled
touch protos_compiled/__init__.py
```

```
pip install grpcio-tools

python -m grpc_tools.protoc \
  --python_out=protos_compiled --pyi_out=protos_compiled --grpc_python_out=protos_compiled \
  --proto_path=./protos \
  ./protos/*.proto
```

### 2. cài các package grpc:

`pip install grpcio protobuf`


### 3. code client và chạy code

`python run_streaming.py`

hãy lưu ý set đúng các auth tokens và đường dẫn file audio để chạy được



tài liệu tham khảo thêm về grpc python: https://grpc.io/docs/languages/python/basics/
