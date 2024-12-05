package com.myapps.pacman.modules.qualifiers

import javax.inject.Qualifier

// A anotação @DispatcherIO é um qualificador customizado para indicar que a dependência fornecida
// deve ser associada ao dispatcher de IO (Input/Output) em operações de fundo, como leitura e gravação
// de dados.
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DispatcherIO

// A anotação @DispatcherDefault é um qualificador customizado para indicar que a dependência fornecida
// deve ser associada ao dispatcher padrão para execução de tarefas em segundo plano que não se enquadram
// em operações de IO, mas podem envolver cálculos ou operações gerais.
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DispatcherDefault
