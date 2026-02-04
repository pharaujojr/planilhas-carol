# Nota Compare

Aplicação Spring Boot simples para comparar duas planilhas Excel:
- `sefaz` (planilha partida): tabela começa na linha 7 (cabeçalho na linha 7), coluna C é a nota
- `sistema` (planilha final): tabela começa na linha 3 (cabeçalho na linha 3), coluna B é a nota

Normalização: extrai a sequência de dígitos (usa a última sequência encontrada) e compara valores numéricos (ex.: `1216` igual a `NF-e 000001216`).

Como rodar localmente (porta 8989):

Com Gradle instalado:
```bash
cd /home/paulo/PROJETOS/CAROL_ANALISE
./gradlew bootRun
```

Abra http://localhost:4679 e faça upload das duas planilhas.

Rodando com Docker Compose:

```bash
cd /home/paulo/PROJETOS/CAROL_ANALISE
docker compose up --build
```

O serviço ficará disponível em http://localhost:4679
