FROM quorumengineering/quorum:latest

ARG VALIDATOR

WORKDIR node

RUN mkdir -p /data/keystore

COPY node/goQuorum/genesis.json ./data/genesis.json
COPY node/goQuorum/static-nodes.json ./data/static-nodes.json


COPY node/${VALIDATOR}/account* ./data/keystore/
COPY node/${VALIDATOR}/address ./data/
COPY node/${VALIDATOR}/nodekey* ./data/

RUN geth --datadir ./data init ./data/genesis.json

ENTRYPOINT ["geth"]