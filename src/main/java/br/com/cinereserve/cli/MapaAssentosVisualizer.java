package br.com.cinereserve.cli;

import br.com.cinereserve.model.Assento;
import br.com.cinereserve.model.Sessao;
import br.com.cinereserve.model.TipoAssento;

import java.util.*;

/**
 * Utilitário para exibição visual do mapa de assentos de uma sessão no terminal.
 */
public class MapaAssentosVisualizer {

    public static void exibirMapa(Sessao sessao) {
        System.out.println("\n========================================================");
        System.out.println("            TELA DO CINEMA (FRENTE DA SALA)             ");
        System.out.println("--------------------------------------------------------");

        // Agrupa os assentos por fila ordenada
        Map<Character, List<Assento>> assentosPorFila = new TreeMap<>();
        for (Assento assento : sessao.getAssentos().values()) {
            assentosPorFila.computeIfAbsent(assento.getFila(), f -> new ArrayList<>()).add(assento);
        }

        for (Map.Entry<Character, List<Assento>> entry : assentosPorFila.entrySet()) {
            char fila = entry.getKey();
            List<Assento> assentosFila = entry.getValue();
            assentosFila.sort(Comparator.comparingInt(Assento::getNumero));

            System.out.printf(" Fila %c:  ", fila);
            for (Assento a : assentosFila) {
                boolean ocupado = !sessao.estaDisponivel(a.getCodigo());
                if (ocupado) {
                    System.out.printf("[ X:%-3s] ", a.getCodigo());
                } else if (a.getTipo() == TipoAssento.VIP) {
                    System.out.printf("[*V:%-3s] ", a.getCodigo());
                } else if (a.getTipo() == TipoAssento.PNE) {
                    System.out.printf("[ P:%-3s] ", a.getCodigo());
                } else {
                    System.out.printf("[ L:%-3s] ", a.getCodigo());
                }
            }
            System.out.println();
        }

        System.out.println("--------------------------------------------------------");
        System.out.println("Legenda: [ L:..] Comum Livre | [*V:..] VIP Livre | [ P:..] PNE | [ X:..] Ocupado");
        System.out.printf("Total: %d | Disponíveis: %d | Ocupados: %d\n",
                sessao.getTotalAssentos(), sessao.getAssentosDisponiveisCount(), sessao.getAssentosOcupados().size());
        System.out.println("========================================================\n");
    }
}
