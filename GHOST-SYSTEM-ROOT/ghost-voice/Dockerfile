FROM python:3.12-slim

RUN apt-get update && apt-get install -y \
    libsndfile1 \
    libasound2 \
    libstdc++6 \
    tar \
    findutils \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
RUN pip install --no-cache-dir flask
COPY . .

# Extração e GARANTIA de que não existam arquivos fantasmas
RUN if [ -f ./piper/piper_linux_x86_64.tar.gz ]; then \
        tar -xzf ./piper/piper_linux_x86_64.tar.gz -C . && \
        rm ./piper/piper_linux_x86_64.tar.gz; \
    fi

# Garante permissão de execução
RUN find /app -type f -name "piper" -not -name "*.*" -exec chmod +x {} +

# IMPORTANTE: Define a biblioteca para o ONNX não crashar
ENV LD_LIBRARY_PATH="/app/piper:${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}"

EXPOSE 5000
CMD ["python", "main.py"]