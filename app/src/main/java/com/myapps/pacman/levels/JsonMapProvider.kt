package com.myapps.pacman.levels

import android.content.Context
import com.myapps.pacman.utils.Position
import org.json.JSONObject

// A classe JsonMapProvider implementa a interface MapProvider, sendo responsável por carregar e fornecer
// os dados de mapas e níveis a partir de arquivos JSON. Ela usa o contexto do Android para acessar
// os arquivos dentro do diretório 'assets'.
class JsonMapProvider(private val context: Context): MapProvider {

    // Função que retorna um mapa contendo os dados de cada nível, mapeando o índice do nível para os dados
    // correspondentes do início do nível.
    override fun getMaps(): Map<Int, LevelStartData> {
        // Carrega os arquivos JSON que contêm os dados dos mapas e níveis.
        val mapsJson = loadJsonFromAsset("levels_maps.json", context)
        val levelsJson = loadJsonFromAsset("levels_data.json", context)

        // Verifica se algum dos arquivos JSON não foi carregado corretamente e retorna um mapa vazio.
        if (mapsJson == null || levelsJson == null) return emptyMap()

        // Faz o parsing dos arquivos JSON carregados.
        val listsOfMaps = parseMapsJson(mapsJson)
        val listOfLevelDefaults = parseLevelsJson(levelsJson)

        // Cria um mapa mutável para armazenar os dados dos níveis.
        val mapOfLevels = mutableMapOf<Int, LevelStartData>()

        // Associa os layouts dos mapas aos dados do nível, criando uma nova instância de LevelStartData
        // com os dados de mapa atualizados.
        for ((index, mapLayouts) in listsOfMaps) {
            val levelData = listOfLevelDefaults.getOrNull(index)

            // Se os dados do nível existirem, eles são atualizados com o layout do mapa e adicionados ao
            // mapa final de níveis.
            if (levelData != null) {
                val updatedLevelData = levelData.copy(
                    mapCharData = mapLayouts
                )
                mapOfLevels[index] = updatedLevelData
            }
        }

        // Retorna o mapa de níveis completos, associando o índice do nível ao seu respectivo dado de início.
        return mapOfLevels
    }

    // Função auxiliar para carregar um arquivo JSON da pasta 'assets' e retornar seu conteúdo como uma String.
    private fun loadJsonFromAsset(fileName: String, context: Context): String? {
        return try {
            // Abre o arquivo e lê seu conteúdo.
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ex: Exception) {
            // Se houver um erro ao carregar o arquivo, retorna null.
            null
        }
    }

    // Função que faz o parsing do arquivo JSON de mapas, transformando-o em um Map onde a chave é
    // o índice do mapa e o valor é a lista de strings representando o layout do mapa.
    private fun parseMapsJson(jsonString: String?): Map<Int, List<String>> {
        if (jsonString == null) return emptyMap()
        val maps = mutableMapOf<Int, List<String>>()
        val jsonObject = JSONObject(jsonString)
        val mapsJsonArray = jsonObject.getJSONArray("maps")

        // Itera sobre cada mapa no arquivo JSON e cria um layout a partir dos dados.
        for (i in 0 until mapsJsonArray.length()) {
            val mapJsonData = mapsJsonArray.getJSONObject(i)
            val layoutArray = mapJsonData.getJSONArray("layout")
            val layoutList = mutableListOf<String>()
            for (j in 0 until layoutArray.length()) {
                layoutList.add(layoutArray.getString(j))
            }
            maps[i] = layoutList
        }
        return maps
    }

    // Função que faz o parsing do arquivo JSON de dados dos níveis, retornando uma lista de objetos
    // LevelStartData com as informações do nível, como posições dos personagens, dimensões do mapa, etc.
    private fun parseLevelsJson(jsonString: String?): List<LevelStartData> {
        if (jsonString == null) return emptyList()
        val listOfLevelsDefaults = mutableListOf<LevelStartData>()
        val jsonObject = JSONObject(jsonString)
        val levelsJson = jsonObject.getJSONArray("levels")

        // Itera sobre cada nível no arquivo JSON e cria um objeto LevelStartData com os dados
        // correspondentes a esse nível.
        for (i in 0 until levelsJson.length()) {
            val levelJson = levelsJson.getJSONObject(i)
            val levelStartData = LevelStartData(
                pacmanDefaultPosition = parsePosition(levelJson.getJSONObject("pacmanDefaultPosition")),
                blinkyDefaultPosition = parsePosition(levelJson.getJSONObject("blinkyDefaultPosition")),
                inkyDefaultPosition = parsePosition(levelJson.getJSONObject("inkyDefaultPosition")),
                pinkyDefaultPosition = parsePosition(levelJson.getJSONObject("pinkyDefaultPosition")),
                clydeDefaultPosition = parsePosition(levelJson.getJSONObject("clydeDefaultPosition")),
                homeTargetPosition = parsePosition(levelJson.getJSONObject("homeTargetPosition")),
                ghostHomeXRange = levelJson.getJSONArray("ghostHomeXRange").let {
                    IntRange(it.getInt(0), it.getInt(it.length() - 1))
                },
                ghostHomeYRange = levelJson.getJSONArray("ghostHomeYRange").let {
                    IntRange(it.getInt(0), it.getInt(it.length() - 1))
                },
                blinkyScatterPosition = parsePosition(levelJson.getJSONObject("blinkyScatterPosition")),
                inkyScatterPosition = parsePosition(levelJson.getJSONObject("inkyScatterPosition")),
                pinkyScatterPosition = parsePosition(levelJson.getJSONObject("pinkyScatterPosition")),
                clydeScatterPosition = parsePosition(levelJson.getJSONObject("clydeScatterPosition")),
                doorTarget = parsePosition(levelJson.getJSONObject("doorTarget")),
                width = levelJson.getInt("width"),
                height = levelJson.getInt("height"),
                amountOfFood = levelJson.getInt("amountOfFood"),
                blinkySpeedDelay = levelJson.getInt("blinkySpeedDelay"),
                isBell = levelJson.getBoolean("isBell")
            )
            listOfLevelsDefaults.add(levelStartData)
        }
        return listOfLevelsDefaults
    }

    // Função auxiliar para parsear uma posição a partir de um JSONObject.
    private fun parsePosition(jsonObject: JSONObject): Position {
        return Position(
            positionX = jsonObject.getInt("x"),
            positionY = jsonObject.getInt("y")
        )
    }
}
