package com.myapps.pacman.levels

import com.myapps.pacman.utils.Position

// A classe LevelStartData contém todas as informações necessárias para iniciar um nível no jogo Pac-Man.
// Essas informações incluem o layout do mapa, as posições iniciais de Pac-Man e os fantasmas, os limites de
// movimento dos fantasmas, o número de alimentos e várias outras propriedades que configuram o nível.
data class LevelStartData(
    // Lista de strings representando o layout do mapa do nível, onde cada string pode representar uma linha
    // do mapa (como paredes, caminhos, alimentos, etc.).
    var mapCharData: List<String> = emptyList(),

    // Posições iniciais dos personagens (Pac-Man e fantasmas).
    val pacmanDefaultPosition: Position,      // Posição inicial de Pac-Man.
    val blinkyDefaultPosition: Position,      // Posição inicial do Blinky (fantasma vermelho).
    val inkyDefaultPosition: Position,        // Posição inicial do Inky (fantasma ciano).
    val pinkyDefaultPosition: Position,       // Posição inicial do Pinky (fantasma rosa).
    val clydeDefaultPosition: Position,       // Posição inicial do Clyde (fantasma laranja).

    // Posições do objetivo para o Pac-Man (casa do fantasma, alvo da porta, etc.).
    val homeTargetPosition: Position,         // Posição do alvo de retorno dos fantasmas (casa dos fantasmas).

    // Intervalos de movimento dos fantasmas em direção à casa dos fantasmas.
    val ghostHomeXRange: IntRange,            // Intervalo de X (horizontal) da área dos fantasmas.
    val ghostHomeYRange: IntRange,            // Intervalo de Y (vertical) da área dos fantasmas.

    // Posições para os fantasmas espalharem-se durante o modo "scatter" (espalhamento).
    val blinkyScatterPosition: Position,      // Posição de espalhamento do Blinky.
    val inkyScatterPosition: Position,        // Posição de espalhamento do Inky.
    val pinkyScatterPosition: Position,       // Posição de espalhamento do Pinky.
    val clydeScatterPosition: Position,       // Posição de espalhamento do Clyde.

    // Posição do alvo da porta (onde Pac-Man pode ir para avançar para o próximo nível).
    val doorTarget: Position,                 // Posição da porta de saída.

    // Dimensões do nível (largura e altura do mapa).
    val width: Int,                           // Largura do mapa.
    val height: Int,                          // Altura do mapa.

    // Número de pedaços de comida (dot) presentes no nível.
    val amountOfFood: Int,                    // Quantidade total de alimentos no nível.

    // Atraso de velocidade do Blinky (quanto mais alto, mais devagar ele se move).
    val blinkySpeedDelay: Int,                // Atraso na velocidade do Blinky.

    // Indica se há um sino no nível (usado em alguns jogos para itens especiais ou modificadores).
    val isBell: Boolean                       // Se o nível tem um sino (geralmente relacionado a bônus ou desafios especiais).
)
