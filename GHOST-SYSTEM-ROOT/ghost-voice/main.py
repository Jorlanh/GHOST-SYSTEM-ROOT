from flask import Flask, request, send_file
import subprocess
import io
import os
import wave
import re

app = Flask(__name__)

# --- SISTEMA DE RASTREAMENTO GHOST ---
def locate_assets():
    target_bin = None
    target_model = None
    
    print("GHOST >> Iniciando varredura de ativos...")
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

@app.route('/speak', methods=['GET'])
def speak():
    text = request.args.get('text', '')
    if not text:
        return "GHOST >> Erro: Texto ausente.", 400
    
    if not PIPER_EXE or not MODEL_FILE:
        return "GHOST >> Erro: Ativos corrompidos ou ausentes.", 500

    # Sanitização: Mantém a limpeza para não quebrar a síntese
    clean_text = re.sub(r'[^\w\s.,!?;:]', '', text)
    
    # --- PROTOCOLO BAN (A Raposa Imortal) ---
    # Usamos noise_scale alto (0.8) para dar mais "vida" e deboche à voz.
    # length_scale em 1.0 para manter o ritmo de fala malandro, nem lento, nem rápido.
    command = [
        PIPER_EXE,
        "--model", MODEL_FILE,
        "--length_scale", "1.15",   # Um pouco mais lento, fala arrastada
        "--noise_scale", "0.75",    # Mais alto que o padrão, mas sem distorcer
        "--noise_w", "0.9",         # Relaxamento na pronúncia
        "--output_raw"
    ]
    
    try:
        process = subprocess.Popen(
            command, 
            stdin=subprocess.PIPE, 
            stdout=subprocess.PIPE, 
            stderr=subprocess.PIPE,
            bufsize=0
        )
        
        audio_raw, err = process.communicate(input=f"{clean_text}\n".encode('utf-8'))
        
        if process.returncode != 0:
            return f"GHOST >> Piper Error: {err.decode()}", 500

        # --- AJUSTE ACÚSTICO BAN ---
        buffer = io.BytesIO()
        with wave.open(buffer, 'wb') as wav_file:
            wav_file.setnchannels(1)
            wav_file.setsampwidth(2)
            
            # Para o Ban, usamos 22050Hz (Frequência Real). 
            # Isso deixa a voz mais "viva" e menos "pesada" que a do Jarvis.
            wav_file.setframerate(21000)  
            wav_file.writeframes(audio_raw)
        
        buffer.seek(0)
        print(f"GHOST >> Sintetizado (Ban Mode): '{clean_text[:30]}...'")
        
        return send_file(buffer, mimetype="audio/wav")
        
    except Exception as e:
        return f"GHOST >> System Failure: {str(e)}", 500

@app.route('/status')
def status():
    return {
        "status": "online",
        "mode": "Ban (The Fox Sin of Greed)",
        "engine": "GHOST-VOICE-PIPER"
    }

if __name__ == '__main__':
    print("GHOST >> Córtex Vocal (BAN) Ativo na porta 5000")
    app.run(host='0.0.0.0', port=5000, threaded=True)