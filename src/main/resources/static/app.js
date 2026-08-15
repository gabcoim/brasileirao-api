const LIGA_ID = 71;
const parametrosPagina = new URLSearchParams(window.location.search);
const temporadaSolicitada = Number(parametrosPagina.get("temporada"));

const seletorTemporada = document.querySelector("#temporada");
const avisoStatus = document.querySelector("#aviso-status");
const listaRodadas = document.querySelector("#lista-rodadas");
const botaoRecarregar = document.querySelector("#recarregar");
const totalPartidas = document.querySelector("#total-partidas");
const totalTimes = document.querySelector("#total-times");
const tituloClassificacao = document.querySelector("#titulo-classificacao");
const corpoClassificacao = document.querySelector("#corpo-classificacao");
const navegacaoRodadas = document.querySelector("#navegacao-rodadas");
const botaoRodadaAnterior = document.querySelector("#rodada-anterior");
const botaoProximaRodada = document.querySelector("#proxima-rodada");
const seletorRodada = document.querySelector("#seletor-rodada");

let partidasCarregadas = [];
let rodadasDisponiveis = [];
let indiceRodadaAtual = 0;
let identificadorCarregamento = 0;

// A Promise também fica no cache para impedir duas requisições iguais ao mesmo tempo.
const partidasPorTemporada = new Map();

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

function buscarPartidasDaTemporada(temporada, forcarAtualizacao = false) {
    const chave = String(temporada);

    if (forcarAtualizacao) {
        partidasPorTemporada.delete(chave);
    }

    if (!partidasPorTemporada.has(chave)) {
        const carregamento = buscarJson(
            `/partidas?ligaId=${LIGA_ID}&temporada=${encodeURIComponent(chave)}`
        ).catch(erro => {
            // Uma falha não deve ficar guardada: a próxima tentativa poderá buscar novamente.
            partidasPorTemporada.delete(chave);
            throw erro;
        });

        partidasPorTemporada.set(chave, carregamento);
    }

    return partidasPorTemporada.get(chave);
}

function preCarregarTemporadas(temporadas, temporadaAtual) {
    temporadas
        .filter(temporada => String(temporada) !== String(temporadaAtual))
        .forEach(temporada => {
            buscarPartidasDaTemporada(temporada).catch(erro => {
                // O pré-carregamento é opcional e não deve exibir erro para o usuário.
                console.warn(`Não foi possível pré-carregar ${temporada}.`, erro);
            });
        });
}

async function carregarTemporadas() {
    definirAviso("Buscando temporadas disponíveis...");

    try {
        const temporadas = await buscarJson(`/partidas/temporadas?ligaId=${LIGA_ID}`);

        seletorTemporada.innerHTML = "";

        if (temporadas.length === 0) {
            seletorTemporada.innerHTML = "<option>Nenhuma</option>";
            definirAviso("Nenhuma temporada foi importada para esta liga.");
            return;
        }

        temporadas.forEach(temporada => {
            const opcao = document.createElement("option");
            opcao.value = temporada;
            opcao.textContent = temporada;
            seletorTemporada.appendChild(opcao);
        });

        if (temporadas.includes(temporadaSolicitada)) {
            seletorTemporada.value = temporadaSolicitada;
        }

        seletorTemporada.disabled = false;
        await carregarPartidas();
        preCarregarTemporadas(temporadas, seletorTemporada.value);
    } catch (erro) {
        definirAviso(
            "Não foi possível carregar as temporadas. Confira se o Spring Boot está em execução.",
            true
        );
        console.error(erro);
    }
}

async function carregarPartidas(forcarAtualizacao = false) {
    const temporada = seletorTemporada.value;
    const carregamentoAtual = ++identificadorCarregamento;
    tituloClassificacao.textContent = `Classificação final - Temporada ${temporada}`;
    definirAviso(`Carregando partidas de ${temporada}...`);
    listaRodadas.innerHTML = "";
    navegacaoRodadas.hidden = true;

    try {
        const partidas = await buscarPartidasDaTemporada(
            temporada,
            forcarAtualizacao
        );

        // Se o usuário já escolheu outra temporada, esta resposta antiga é ignorada.
        if (carregamentoAtual !== identificadorCarregamento) {
            return;
        }

        atualizarResumo(partidas);
        exibirClassificacao(partidas);

        if (partidas.length === 0) {
            definirAviso(`Nenhuma partida encontrada para ${temporada}.`);
            return;
        }

        definirAviso("");
        prepararNavegacaoRodadas(partidas);
    } catch (erro) {
        if (carregamentoAtual !== identificadorCarregamento) {
            return;
        }

        definirAviso("Não foi possível carregar as partidas desta temporada.", true);
        console.error(erro);
    }
}

function prepararNavegacaoRodadas(partidas) {
    partidasCarregadas = partidas;
    rodadasDisponiveis = [...new Set(
        partidas
            .map(partida => partida.rodada)
            .filter(Number.isInteger)
    )].sort((a, b) => a - b);
    indiceRodadaAtual = 0;
    navegacaoRodadas.hidden = rodadasDisponiveis.length === 0;

    seletorRodada.innerHTML = "";
    rodadasDisponiveis.forEach(numeroRodada => {
        const opcao = document.createElement("option");
        opcao.value = numeroRodada;
        opcao.textContent = numeroRodada;
        seletorRodada.appendChild(opcao);
    });

    exibirRodadaAtual();
}

function exibirRodadaAtual() {
    listaRodadas.innerHTML = "";

    if (rodadasDisponiveis.length === 0) {
        return;
    }

    const numeroRodada = rodadasDisponiveis[indiceRodadaAtual];
    const partidasDaRodada = partidasCarregadas.filter(
        partida => partida.rodada === numeroRodada
    );

    seletorRodada.value = numeroRodada;
    botaoRodadaAnterior.disabled = indiceRodadaAtual === 0;
    botaoProximaRodada.disabled = indiceRodadaAtual === rodadasDisponiveis.length - 1;
    exibirRodadas(partidasDaRodada);
}

function atualizarResumo(partidas) {
    const idsTimes = new Set();

    partidas.forEach(partida => {
        idsTimes.add(partida.mandante.idExterno);
        idsTimes.add(partida.visitante.idExterno);
    });

    totalPartidas.textContent = partidas.length;
    totalTimes.textContent = idsTimes.size;
}

function exibirClassificacao(partidas) {
    const classificacao = calcularClassificacao(partidas);
    corpoClassificacao.innerHTML = "";

    classificacao.forEach((time, indice) => {
        const linha = document.createElement("tr");
        linha.className = obterFaixaClassificacao(indice);
        const saldoClasse = time.sg > 0
            ? "saldo-positivo"
            : time.sg < 0 ? "saldo-negativo" : "";

        linha.innerHTML = `
            <td class="posicao">${indice + 1}</td>
            <td class="clube-celula"></td>
            <td class="pontos">${time.pts}</td>
            <td>${time.j}</td>
            <td>${time.v}</td>
            <td>${time.e}</td>
            <td>${time.d}</td>
            <td>${time.gp}</td>
            <td>${time.gc}</td>
            <td class="${saldoClasse}">${time.sg}</td>
        `;

        const clube = criarTimeTabela(time);
        linha.querySelector(".clube-celula").appendChild(clube);
        corpoClassificacao.appendChild(linha);
    });
}

function obterFaixaClassificacao(indice) {
    const posicao = indice + 1;

    if (posicao === 1) {
        return "faixa-campeao";
    }
    if (posicao >= 2 && posicao <= 6) {
        return "faixa-libertadores";
    }
    if (posicao >= 17 && posicao <= 20) {
        return "faixa-rebaixamento";
    }
    return "";
}

function calcularClassificacao(partidas) {
    const times = new Map();

    partidas.forEach(partida => {
        garantirTimeNaClassificacao(times, partida.mandante);
        garantirTimeNaClassificacao(times, partida.visitante);

        const partidaValida = partida.status === "FINALIZADA"
            && Number.isInteger(partida.placarMandante)
            && Number.isInteger(partida.placarVisitante);

        if (!partidaValida) {
            return;
        }

        const mandante = times.get(partida.mandante.idExterno);
        const visitante = times.get(partida.visitante.idExterno);
        const golsMandante = partida.placarMandante;
        const golsVisitante = partida.placarVisitante;

        mandante.j++;
        visitante.j++;
        mandante.gp += golsMandante;
        mandante.gc += golsVisitante;
        visitante.gp += golsVisitante;
        visitante.gc += golsMandante;

        if (golsMandante > golsVisitante) {
            mandante.v++;
            mandante.pts += 3;
            visitante.d++;
        } else if (golsMandante < golsVisitante) {
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
        .sort((a, b) =>
            b.pts - a.pts
            || b.v - a.v
            || b.sg - a.sg
            || b.gp - a.gp
            || a.nome.localeCompare(b.nome, "pt-BR")
        );
}

function garantirTimeNaClassificacao(times, time) {
    if (!times.has(time.idExterno)) {
        times.set(time.idExterno, {
            ...time,
            pts: 0,
            j: 0,
            v: 0,
            e: 0,
            d: 0,
            gp: 0,
            gc: 0
        });
    }
}

function criarTimeTabela(time) {
    const elemento = document.createElement("a");
    elemento.className = "clube-tabela";
    elemento.href = construirUrlTime(time.idExterno);

    if (time.escudoUrl) {
        const imagem = document.createElement("img");
        imagem.src = time.escudoUrl;
        imagem.alt = "";
        imagem.loading = "lazy";
        elemento.appendChild(imagem);
    } else {
        const placeholder = document.createElement("span");
        placeholder.className = "escudo-placeholder";
        placeholder.textContent = obterIniciais(time.nome);
        elemento.appendChild(placeholder);
    }

    const nome = document.createElement("span");
    nome.textContent = time.nome;
    elemento.appendChild(nome);
    return elemento;
}

function exibirRodadas(partidas) {
    const grupos = new Map();

    partidas.forEach(partida => {
        const rodada = partida.rodada ?? "outras";
        const grupo = grupos.get(rodada) ?? [];
        grupo.push(partida);
        grupos.set(rodada, grupo);
    });

    for (const [rodada, partidasDaRodada] of grupos) {
        const secao = document.createElement("section");
        secao.className = "rodada";

        const titulo = rodada === "outras" ? "Outras partidas" : `Rodada ${rodada}`;
        secao.innerHTML = `
            <header class="rodada-cabecalho">
                <h3>${titulo}</h3>
                <span>${partidasDaRodada.length} ${partidasDaRodada.length === 1 ? "jogo" : "jogos"}</span>
            </header>
            <div class="partidas"></div>
        `;

        const containerPartidas = secao.querySelector(".partidas");
        partidasDaRodada.forEach(partida => {
            containerPartidas.appendChild(criarPartida(partida));
        });

        listaRodadas.appendChild(secao);
    }
}

function criarPartida(partida) {
    const elemento = document.createElement("article");
    elemento.className = "partida";

    const mandante = criarTime(partida.mandante, "mandante");
    const visitante = criarTime(partida.visitante, "visitante");
    const placarMandante = partida.placarMandante ?? "–";
    const placarVisitante = partida.placarVisitante ?? "–";
    const status = nomesStatus[partida.status] ?? partida.status;
    const data = formatarData(partida.dataHora);

    elemento.appendChild(mandante);

    const placar = document.createElement("div");
    placar.className = "placar-area";
    placar.innerHTML = `
        <span class="placar">${placarMandante} × ${placarVisitante}</span>
        <span class="detalhe-partida">${escaparHtml(status)} · ${escaparHtml(data)}</span>
    `;
    elemento.appendChild(placar);
    elemento.appendChild(visitante);

    return elemento;
}

function criarTime(time, lado) {
    const elemento = document.createElement("a");
    elemento.className = `time ${lado}`;
    elemento.href = construirUrlTime(time.idExterno);

    if (time.escudoUrl) {
        const imagem = document.createElement("img");
        imagem.src = time.escudoUrl;
        imagem.alt = `Escudo do ${time.nome}`;
        imagem.loading = "lazy";
        elemento.appendChild(imagem);
    } else {
        const placeholder = document.createElement("span");
        placeholder.className = "escudo-placeholder";
        placeholder.textContent = obterIniciais(time.nome);
        elemento.appendChild(placeholder);
    }

    const nome = document.createElement("span");
    nome.textContent = time.nome;
    elemento.appendChild(nome);
    return elemento;
}

function formatarData(dataHora) {
    if (!dataHora) {
        return "Data a definir";
    }

    return new Intl.DateTimeFormat("pt-BR", {
        day: "2-digit",
        month: "short",
        hour: "2-digit",
        minute: "2-digit"
    }).format(new Date(dataHora));
}

function obterIniciais(nome = "") {
    return nome
        .split(" ")
        .slice(0, 2)
        .map(parte => parte.charAt(0))
        .join("")
        .toUpperCase();
}

function construirUrlTime(timeId) {
    const temporada = encodeURIComponent(seletorTemporada.value);
    return `/time.html?timeId=${encodeURIComponent(timeId)}&temporada=${temporada}`;
}

function escaparHtml(valor) {
    const elemento = document.createElement("span");
    elemento.textContent = valor ?? "";
    return elemento.innerHTML;
}

function definirAviso(mensagem, erro = false) {
    avisoStatus.textContent = mensagem;
    avisoStatus.classList.toggle("erro", erro);
}

seletorTemporada.addEventListener("change", () => {
    const temporada = encodeURIComponent(seletorTemporada.value);
    window.history.replaceState(null, "", `/?temporada=${temporada}`);
    carregarPartidas();
});
botaoRecarregar.addEventListener("click", () => carregarPartidas(true));
seletorRodada.addEventListener("change", () => {
    const rodadaSelecionada = Number(seletorRodada.value);
    const novoIndice = rodadasDisponiveis.indexOf(rodadaSelecionada);

    if (novoIndice >= 0) {
        indiceRodadaAtual = novoIndice;
        exibirRodadaAtual();
    }
});
botaoRodadaAnterior.addEventListener("click", () => {
    if (indiceRodadaAtual > 0) {
        indiceRodadaAtual--;
        exibirRodadaAtual();
    }
});
botaoProximaRodada.addEventListener("click", () => {
    if (indiceRodadaAtual < rodadasDisponiveis.length - 1) {
        indiceRodadaAtual++;
        exibirRodadaAtual();
    }
});
carregarTemporadas();
