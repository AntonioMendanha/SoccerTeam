import com.fasterxml.jackson.databind.ObjectMapper;
import dtos.ClassificacaoDTO;
import dtos.TimeDTO;
import org.apache.commons.lang3.StringUtils;
import utils.Config;
import utils.SoccerUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SoccerMainApplication {

    public static void main(String[] args) throws IOException {

        List<ClassificacaoDTO> classificacao = new ArrayList<>();

        Files.lines(SoccerUtils.getPath(Config.FIRST_FILE), StandardCharsets.UTF_8)
                .filter(StringUtils::isNotBlank) /*Removendo Linhas em brancas*/
                .distinct() /* Removendo linhas repetidas */
                .map(field -> /* Formartando linha para TimedTO */
                        new ObjectMapper().convertValue(SoccerUtils.convertToMap(field.split(";")), TimeDTO.class)
                ).sorted(Comparator.comparing(TimeDTO::getDay, Comparator.reverseOrder()))/* Ordenando baseado no DataHora */

                //Subdivida a estrutura de dados por time (mandante)
                .collect(
                        Collectors.groupingBy(TimeDTO::getHome))
                .forEach((key, dtos) -> {
                    try {
                        /* Imprimindo o histórico de cada time */
                        Files.write(SoccerUtils.getPath(key), SoccerUtils.convertToListString(dtos), StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    classificacao.add(SoccerUtils.getClassificacao(dtos, key));
                });

        var listClassificacao = classificacao.stream()
                .sorted(Comparator.comparing(ClassificacaoDTO::getPoints, Comparator.reverseOrder()).thenComparing(ClassificacaoDTO::getWins, Comparator.reverseOrder()))
                .map(time -> time.toString()).collect(Collectors.toList());

        Files.write(SoccerUtils.getPath(Config.CLASSIFICATION_FILE), listClassificacao, StandardCharsets.UTF_8);
    }
}
