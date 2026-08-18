const LIGA_ID = 71;
const parametros = new URLSearchParams(window.location.search);
const timeId = Number(parametros.get("timeId"));
const temporadaInicial = Number(parametros.get("temporada"));

const seletorTemporada = document.querySelector("#temporada-time");
const voltarClassificacao = document.querySelector("#voltar-classificacao");
const aviso = document.querySelector("#aviso-time");
const conteudo = document.querySelector("#conteudo-time");
const timeNome = document.querySelector("#time-nome");
const timeDescricao = document.querySelector("#time-descricao");
const timeEscudo = document.querySelector("#time-escudo");
const listaJogos = document.querySelector("#lista-jogos-time");
const quantidadeJogos = document.querySelector("#quantidade-jogos-time");
const seletorRodadaTime =
    document.querySelector("#seletor-rodada-time");
let jogosDoTimeCarregados = [];

const camposEstatisticas = {
    posicao: document.querySelector("#time-posicao"),
    pontos: document.querySelector("#time-pontos"),
    jogos: document.querySelector("#time-jogos"),
    vitorias: document.querySelector("#time-vitorias"),
    empates: document.querySelector("#time-empates"),
    derrotas: document.querySelector("#time-derrotas"),
    saldo: document.querySelector("#time-saldo")
};

const nomesStatus = {
    AGENDADA: "Agendada",
    EM_ANDAMENTO: "Em andamento",
    FINALIZADA: "Finalizada",
    ADIADA: "Adiada",
    CANCELADA: "Cancelada",
    INTERROMPIDA: "Interrompida",
    DESCONHECIDA: "Status desconhecido"
};

async function buscarJson(url) {
    const resposta = await fetch(url);
    if (!resposta.ok) {
        throw new Error(`O backend respondeu com o status ${resposta.status}.`);
    }
    return resposta.json();
}

async function iniciar() {
    if (!Number.isInteger(timeId) || timeId <= 0) {
        mostrarErro("O endereço não contém um ID de time válido.");
        return;
    }

    try {
        const temporadas = await buscarJson(`/partidas/temporadas?ligaId=${LIGA_ID}`);
        seletorTemporada.innerHTML = "";

        if (temporadas.length === 0) {
            mostrarErro("Nenhuma temporada foi importada para esta liga.");
            return;
        }

        temporadas.forEach(temporada => {
            const opcao = document.createElement("option");
            opcao.value = temporada;
            opcao.textContent = temporada;
            seletorTemporada.appendChild(opcao);
        });

        if (temporadas.includes(temporadaInicial)) {
            seletorTemporada.value = temporadaInicial;
        }

        seletorTemporada.disabled = temporadas.length === 0;
        await carregarTime();
    } catch (erro) {
        mostrarErro("Não foi possível carregar os dados. Confira se o Spring Boot está em execução.");
        console.error(erro);
    }
}

async function carregarTime() {
    const temporada = Number(seletorTemporada.value);
    aviso.textContent = `Carregando a temporada ${temporada}...`;
    aviso.classList.remove("erro");
    conteudo.hidden = true;
    seletorRodadaTime.disabled = true;
    voltarClassificacao.href = `/?temporada=${temporada}`;

    try {
        const partidas = await buscarJson(
            `/partidas?ligaId=${LIGA_ID}&temporada=${encodeURIComponent(temporada)}`
        );
        const jogosDoTime = partidas.filter(partida => participaDaPartida(partida, timeId));

        if (jogosDoTime.length === 0) {
            mostrarErro(`Este time não possui partidas registradas em ${temporada}.`);
            return;
        }

        const time = obterTime(jogosDoTime[0], timeId);
        jogosDoTimeCarregados = jogosDoTime;
        prepararSeletorRodadas(jogosDoTime);
        preencherCabecalho(time, temporada);
        preencherEstatisticas(time, partidas, jogosDoTime);
        exibirJogos(jogosDoTime);

        quantidadeJogos.textContent = `${jogosDoTime.length} jogos encontrados`;
        aviso.textContent = "";
        conteudo.hidden = false;
    } catch (erro) {
        mostrarErro("Não foi possível carregar os jogos deste time.");
        console.error(erro);
    }
}

function prepararSeletorRodadas(jogos) {
    const rodadas = [...new Set(
        jogos
            .map(jogo => jogo.rodada)
            .filter(Number.isInteger)
    )].sort((a, b) => a - b);

    seletorRodadaTime.innerHTML = '<option value="todas">Todas</option>';

    rodadas.forEach(rodada => {
        const opcao = document.createElement("option");
        opcao.value = rodada;
        opcao.textContent = rodada;
        seletorRodadaTime.appendChild(opcao);
    });

    seletorRodadaTime.value = "todas";
    seletorRodadaTime.disabled = false;
}

function preencherCabecalho(time, temporada) {
    timeNome.textContent = time.nome;
    timeDescricao.textContent = `Campanha e jogos no Brasileirão ${temporada}.`;
    document.title = `${time.nome} — Brasileirão ${temporada}`;
    timeEscudo.innerHTML = "";

    if (time.escudoUrl) {
        const imagem = document.createElement("img");
        imagem.src = time.escudoUrl;
        imagem.alt = `Escudo do ${time.nome}`;
        timeEscudo.appendChild(imagem);
    } else {
        timeEscudo.textContent = obterIniciais(time.nome);
    }
}

function preencherEstatisticas(time, todasPartidas, jogosDoTime) {
    const estatisticas = calcularEstatisticas(time.idExterno, jogosDoTime);
    const classificacao = calcularClassificacao(todasPartidas);
    const posicao = classificacao.findIndex(item => item.idExterno === time.idExterno) + 1;

    camposEstatisticas.posicao.textContent = posicao > 0 ? `${posicao}º` : "—";
    camposEstatisticas.pontos.textContent = estatisticas.pts;
    camposEstatisticas.jogos.textContent = estatisticas.j;
    camposEstatisticas.vitorias.textContent = estatisticas.v;
    camposEstatisticas.empates.textContent = estatisticas.e;
    camposEstatisticas.derrotas.textContent = estatisticas.d;
    camposEstatisticas.saldo.textContent = estatisticas.gp - estatisticas.gc;
}

function calcularEstatisticas(id, partidas) {
    const dados = {pts: 0, j: 0, v: 0, e: 0, d: 0, gp: 0, gc: 0};

    partidas.forEach(partida => {
        if (!partidaFinalizadaComPlacar(partida)) return;

        const mandante = partida.mandante.idExterno === id;
        const golsPro = mandante ? partida.placarMandante : partida.placarVisitante;
        const golsContra = mandante ? partida.placarVisitante : partida.placarMandante;

        dados.j++;
        dados.gp += golsPro;
        dados.gc += golsContra;

        if (golsPro > golsContra) {
            dados.v++;
            dados.pts += 3;
        } else if (golsPro < golsContra) {
            dados.d++;
        } else {
            dados.e++;
            dados.pts++;
        }
    });

    return dados;
}

function calcularClassificacao(partidas) {
    const times = new Map();

    partidas.forEach(partida => {
        [partida.mandante, partida.visitante].forEach(time => {
            if (!times.has(time.idExterno)) {
                times.set(time.idExterno, {
                    ...time, pts: 0, j: 0, v: 0, e: 0, d: 0, gp: 0, gc: 0
                });
            }
        });

        if (!partidaFinalizadaComPlacar(partida)) return;

        const mandante = times.get(partida.mandante.idExterno);
        const visitante = times.get(partida.visitante.idExterno);
        const gm = partida.placarMandante;
        const gv = partida.placarVisitante;

        mandante.j++;
        visitante.j++;
        mandante.gp += gm;
        mandante.gc += gv;
        visitante.gp += gv;
        visitante.gc += gm;

        if (gm > gv) {
            mandante.v++;
            mandante.pts += 3;
            visitante.d++;
        } else if (gm < gv) {
            visitante.v++;
            visitante.pts += 3;
            mandante.d++;
        } else {
            mandante.e++;
            visitante.e++;
            mandante.pts++;
            visitante.pts++;
        }
    });

    return [...times.values()]
        .map(time => ({...time, sg: time.gp - time.gc}))
        .sort((a, b) => b.pts - a.pts || b.v - a.v || b.sg - a.sg || b.gp - a.gp);
}

function exibirJogos(partidas) {
    listaJogos.innerHTML = "";
    const grupos = new Map();

    partidas.forEach(partida => {
        const rodada = partida.rodada ?? "outras";
        const grupo = grupos.get(rodada) ?? [];
        grupo.push(partida);
        grupos.set(rodada, grupo);
    });

    for (const [rodada, jogos] of grupos) {
        const secao = document.createElement("section");
        secao.className = "rodada";
        const titulo = rodada === "outras" ? "Outras partidas" : `Rodada ${rodada}`;
        secao.innerHTML = `
            <header class="rodada-cabecalho">
                <h3>${titulo}</h3>
                <span>${jogos.length} ${jogos.length === 1 ? "jogo" : "jogos"}</span>
            </header>
            <div class="partidas"></div>
        `;
        const container = secao.querySelector(".partidas");
        jogos.forEach(jogo => container.appendChild(criarPartida(jogo)));
        listaJogos.appendChild(secao);
    }
}

function criarPartida(partida) {
    const elemento = document.createElement("article");
    elemento.className = "partida";
    elemento.appendChild(criarTime(partida.mandante, "mandante"));

    const placar = document.createElement("div");
    placar.className = "placar-area";
    const status = nomesStatus[partida.status] ?? partida.status;
    placar.innerHTML = `
        <span class="placar">${partida.placarMandante ?? "–"} × ${partida.placarVisitante ?? "–"}</span>
        <span class="detalhe-partida">${escaparHtml(status)} · ${escaparHtml(formatarData(partida.dataHora))}</span>
    `;
    elemento.appendChild(placar);
    elemento.appendChild(criarTime(partida.visitante, "visitante"));
    return elemento;
}

function criarTime(time, lado) {
    const elemento = document.createElement("a");
    elemento.className = `time ${lado}`;
    if (time.idExterno === timeId) elemento.classList.add("destaque-time");
    elemento.href = `/time.html?timeId=${time.idExterno}&temporada=${seletorTemporada.value}`;

    if (time.escudoUrl) {
        const imagem = document.createElement("img");
        imagem.src = time.escudoUrl;
        imagem.alt = `Escudo do ${time.nome}`;
        imagem.loading = "lazy";
        elemento.appendChild(imagem);
    }

    const nome = document.createElement("span");
    nome.textContent = time.nome;
    elemento.appendChild(nome);
    return elemento;
}

function participaDaPartida(partida, id) {
    return partida.mandante.idExterno === id || partida.visitante.idExterno === id;
}

function obterTime(partida, id) {
    return partida.mandante.idExterno === id ? partida.mandante : partida.visitante;
}

function partidaFinalizadaComPlacar(partida) {
    return partida.status === "FINALIZADA"
        && Number.isInteger(partida.placarMandante)
        && Number.isInteger(partida.placarVisitante);
}

function formatarData(dataHora) {
    if (!dataHora) return "Data a definir";
    return new Intl.DateTimeFormat("pt-BR", {
        day: "2-digit", month: "short", hour: "2-digit", minute: "2-digit"
    }).format(new Date(dataHora));
}

function obterIniciais(nome = "") {
    return nome.split(" ").slice(0, 2).map(parte => parte.charAt(0)).join("").toUpperCase();
}

function escaparHtml(valor) {
    const elemento = document.createElement("span");
    elemento.textContent = valor ?? "";
    return elemento.innerHTML;
}

function mostrarErro(mensagem) {
    aviso.textContent = mensagem;
    aviso.classList.add("erro");
    conteudo.hidden = true;
}

seletorTemporada.addEventListener("change", () => {
    const temporada = seletorTemporada.value;
    window.history.replaceState(null, "", `/time.html?timeId=${timeId}&temporada=${temporada}`);
    carregarTime();
});

seletorRodadaTime.addEventListener("change", () => {
    const rodadaSelecionada = seletorRodadaTime.value;
    const jogosFiltrados = rodadaSelecionada === "todas"
        ? jogosDoTimeCarregados
        : jogosDoTimeCarregados.filter(
            jogo => jogo.rodada === Number(rodadaSelecionada)
        );

    exibirJogos(jogosFiltrados);
    quantidadeJogos.textContent = jogosFiltrados.length === 1
        ? "1 jogo encontrado"
        : `${jogosFiltrados.length} jogos encontrados`;
});

iniciar();
