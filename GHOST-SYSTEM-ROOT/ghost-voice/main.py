from flask import Flask, request, send_file, make_response
import subprocess
import io
import os
import wave
import re
# IMPORTANTE: Você precisará instalar a lib rvc-python no Docker
from rvc_python.infer import VoiceClone

app = Flask(__name__)

@app.after_request
def add_cors_headers(response):
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'Content-Type,Authorization'
    response.headers['Access-Control-Allow-Methods'] = 'GET,PUT,POST,DELETE,OPTIONS'
    return response

def locate_assets():
    target_bin = None
    target_model = None
    print("GHOST >> Varredura de ativos...")
    for root, dirs, files in os.walk('/app'):
        for file in files:
            if file == 'piper' and not file.endswith(('.tar.gz', '.json', '.onnx')):
                path = os.path.join(root, file)
                if os.path.isfile(path) and os.access(path, os.X_OK):
                    target_bin = path
            if file.endswith('.onnx'):
                target_model = os.path.join(root, file)
    return target_bin, target_model

PIPER_EXE, MODEL_FILE = locate_assets()
RVC_MODEL_PATH = "/app/rvc_model/ban.pth"
RVC_INDEX_PATH = "/app/rvc_model/ban.index"

@app.route('/speak', methods=['GET', 'OPTIONS'])
def speak():
    if request.method == 'OPTIONS':
        return '', 204
        
    text = request.args.get('text', '')
    if not text: 
        return "Erro: Texto ausente.", 400

    clean_text = re.sub(r'[^\w\s.,!?;:]', '', text)
    
    # 1. GERA ÁUDIO BASE (PIPER)
    command = [PIPER_EXE, "--model", MODEL_FILE, "--output_raw"]
    process = subprocess.Popen(command, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    audio_raw, _ = process.communicate(input=f"{clean_text}\n".encode('utf-8'))

    # Salva temporário para o RVC processar
    temp_input = "temp_piper.wav"
    with wave.open(temp_input, 'wb') as f:
        f.setnchannels(1); f.setsampwidth(2); f.setframerate(22050)
        f.writeframes(audio_raw)

    # 2. CONVERSÃO DE VOZ (RVC - VEGETA)
    output_rvc = "final_vegeta.wav"
    try:
        rvc = VoiceClone()
        rvc.setup_model(model_path=RVC_MODEL_PATH, index_path=RVC_INDEX_PATH, device="cpu")
        rvc(input_path=temp_input, output_path=output_rvc, pitch_up_key=0, f0method="rmvpe")
        
        # 🔥 GOLPE DE MISERICÓRDIA NO CORS: Injetando direto no arquivo!
        response = make_response(send_file(output_rvc, mimetype="audio/wav"))
        response.headers['Access-Control-Allow-Origin'] = '*'
        return response
        
    except Exception as e:
        err_resp = make_response(f"Erro no RVC: {str(e)}", 500)
        err_resp.headers['Access-Control-Allow-Origin'] = '*'
        return err_resp

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)