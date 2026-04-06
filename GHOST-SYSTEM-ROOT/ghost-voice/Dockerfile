# Usamos 3.10 porque as bibliotecas de RVC são temperamentais com versões novas
FROM python:3.10-slim

# Instala dependências do sistema: áudio, compressão e compiladores
RUN apt-get update && apt-get install -y \
    libsndfile1 \
    libasound2 \
    libstdc++6 \
    ffmpeg \
    build-essential \
    python3-dev \
    tar \
    findutils \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Instala o Flask e o motor de conversão de voz (RVC)
RUN pip install --no-cache-dir flask rvc-python

COPY . .

# Extração do Piper (O motor de fala)
RUN if [ -f ./piper/piper_linux_x86_64.tar.gz ]; then \
        tar -xzf ./piper/piper_linux_x86_64.tar.gz -C . && \
        rm ./piper/piper_linux_x86_64.tar.gz; \
    fi

# Garante que o binário do piper possa ser executado
RUN find /app -type f -name "piper" -not -name "*.*" -exec chmod +x {} +

# Configura o caminho das bibliotecas para o ONNX
ENV LD_LIBRARY_PATH="/app/piper:${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}"

# Cria a pasta do modelo se você esqueceu (mas coloque os arquivos lá!)
RUN mkdir -p /app/rvc_model

EXPOSE 5000

# Acorda o monstro
CMD ["python", "main.py"]