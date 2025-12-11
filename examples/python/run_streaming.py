import sys
import wave
import grpc
from protos_compiled import vnpt_asr_pb2 as rasr
from protos_compiled import vnpt_asr_pb2_grpc as rasr_srv
from protos_compiled import vnpt_audio_pb2 as ra

def listen_print_loop(responses: list[rasr.StreamingRecognizeResponse], show_intermediate=False):
    num_chars_printed = 0
    for response in responses:
        if not response.results:
            continue

        partial_transcript = ""
        for result in response.results:
            if not result.alternatives:
                continue

            transcript = result.alternatives[0].transcript

            if show_intermediate:
                if not result.is_final:
                    partial_transcript += transcript
                else:
                    overwrite_chars = ' ' * (num_chars_printed - len(transcript))
                    print("## " + transcript + overwrite_chars + "\n")
                    num_chars_printed = 0

            else:
                if result.is_final:
                    sys.stdout.buffer.write(transcript.encode('utf-8'))
                    sys.stdout.flush()
                    print("\n")

        if show_intermediate and partial_transcript != "":
            overwrite_chars = ' ' * (num_chars_printed - len(partial_transcript))
            sys.stdout.write(">> " + partial_transcript + overwrite_chars + '\r')
            sys.stdout.flush()
            num_chars_printed = len(partial_transcript) + 3

def request_generator(wf, streaming_config):
    yield rasr.StreamingRecognizeRequest(streaming_config=streaming_config)
    data = wf.readframes(CHUNK)
    while len(data) > 0:
        yield rasr.StreamingRecognizeRequest(audio_content=data)
        data = wf.readframes(CHUNK)



audio_file = '../../audio/4s.wav' # wav file path here
wf = wave.open(audio_file, 'rb')
CHUNK = 2048

server = ''
metadata = (
    ('authorization',''),
    ('token-id',''),
    ('token-key',''),
)

# channel = grpc.insecure_channel(server)
channel = grpc.secure_channel(server, grpc.ssl_channel_credentials())
client = rasr_srv.VnptSpeechRecognitionStub(channel)
config = rasr.RecognitionConfig(
    language_code='vi-VN',
    encoding=ra.AudioEncoding.LINEAR_PCM,
    sample_rate_hertz=wf.getframerate(),
    max_alternatives=1,
    enable_automatic_punctuation=False,
)
streaming_config = rasr.StreamingRecognitionConfig(config=config, interim_results=True)

responses = client.StreamingRecognize(request_generator(wf, streaming_config), metadata=metadata)

listen_print_loop(responses, show_intermediate=True)
print('done')
