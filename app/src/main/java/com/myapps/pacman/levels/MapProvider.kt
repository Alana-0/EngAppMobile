package com.myapps.pacman.levels

// A interface MapProvider define o contrato para fornecer os dados do mapa do jogo Pac-Man.
// Ela obriga as classes que a implementam a fornecer uma implementação do método getMaps(),
// que retorna um mapa com os dados necessários para inicializar os níveis do jogo.
interface MapProvider {

    // O método getMaps retorna um mapa (Map) onde a chave é um inteiro (representando o índice do nível)
    // e o valor é o objeto LevelStartData, que contém os dados de configuração para esse nível.
    // O Map deve conter todos os níveis do jogo e suas respectivas informações de inicialização.
    fun getMaps(): Map<Int, LevelStartData>
}
