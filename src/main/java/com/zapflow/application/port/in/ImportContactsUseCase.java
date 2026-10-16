package com.zapflow.application.port.in;

import java.util.List;

public interface ImportContactsUseCase {

    /**
     * Importa os contatos, substituindo a importação anterior.
     *
     * @param columns nomes das colunas do cabeçalho, já em minúsculas
     * @param rows    linhas de dados do arquivo
     * @throws MissingColumnsException se faltar alguma coluna obrigatória (nada é importado)
     */
    ImportResult importContacts(List<String> columns, List<ContactRow> rows);
}
