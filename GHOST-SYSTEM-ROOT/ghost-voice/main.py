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
            # Localiza o binário real do Piper
            if file == 'piper' and not file.endswith(('.tar.gz', '.json', '.onnx')):
                path = os.path.join(root, file)
                if os.path.isfile(path) and os.access(path, os.X_OK):
                    target_bin = path
            
            # Localiza o modelo ONNX (Córtex Faber)
            if file.endswith('.onnx'):
                target_model = os.path.join(root, file)
                
    return target_bin, target_model

PIPER_EXE, MODEL_FILE = locate_assets()

# Diagnóstico de Inicialização
print("\n--- STATUS DO CÓRTEX GHOST ---")
if PIPER_EXE:
    print(f"GHOST >> Binário Piper: {PIPER_EXE} [READY]")
else:
    print("GHOST >> ERRO: Binário não localizado!")

if MODEL_FILE:
    print(f"GHOST >> Modelo ONNX: {MODEL_FILE} [READY]")
else:
    print("GHOST >> ERRO: Modelo .onnx não localizado!")
print("------------------------------\n")

@app.route('/speak', methods=['GET'])
def speak():
    text = request.args.get('text', '')
    if not text:
        return "GHOST >> Erro: Texto ausente.", 400
    
    if not PIPER_EXE or not MODEL_FILE:
        return "GHOST >> Erro: Ativos corrompidos ou ausentes.", 500

    # Sanitização: Remove caracteres especiais que o modelo pode não suportar
    clean_text = re.sub(r'[^\w\s.,!?;:]', '', text)
    
    # --- PROTOCOLO J.A.R.V.I.S. (Parâmetros Táticos) ---
    command = [
        PIPER_EXE,
        "--model", MODEL_FILE,
        "--length_scale", "1.1",    # Levemente mais calmo e pausado
        "--noise_scale", "0.667",   # Textura aveludada/natural
        "--noise_w", "0.8",         # Estabilidade dos fonemas
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
            error_msg = err.decode()
            print(f"GHOST >> PIPER FAILURE: {error_msg}")
            return f"GHOST >> Piper Error: {error_msg}", 500

        if not audio_raw:
            return "GHOST >> Falha: Áudio gerado está vazio.", 500

        # --- ENGENHARIA DE TOM (Pitch Shift) ---
        buffer = io.BytesIO()
        with wave.open(buffer, 'wb') as wav_file:
            wav_file.setnchannels(1)       # Mono
            wav_file.setsampwidth(2)       # 16-bit
            
            # Frequência Original = 22050. 
            # Baixar para 20000 ou 20500 estica a onda, tornando a voz mais grave.
            # Ajuste este número se quiser mais grave (ex: 19000) ou menos (ex: 21000).
            frequencia_grave = 20000 
            
            wav_file.setframerate(frequencia_grave)   
            wav_file.writeframes(audio_raw)
        
        buffer.seek(0)
        print(f"GHOST >> Sintetizado (Tom Grave): '{clean_text[:30]}...' [{len(audio_raw)} bytes]")
        
        return send_file(
            buffer, 
            mimetype="audio/wav", 
            as_attachment=False, 
            download_name="speech.wav"
        )
        
    except Exception as e:
        print(f"GHOST >> CRITICAL ERROR: {str(e)}")
        return f"GHOST >> System Failure: {str(e)}", 500

@app.route('/status')
def status():
    return {
        "status": "online",
        "binary": PIPER_EXE,
        "model": MODEL_FILE,
        "engine": "GHOST-VOICE-PIPER",
        "mode": "J.A.R.V.I.S. Deep Tone"
    }

if __name__ == '__main__':
    print("GHOST >> Córtex Vocal Ativo na porta 5000")
    app.run(host='0.0.0.0', port=5000, threaded=True)