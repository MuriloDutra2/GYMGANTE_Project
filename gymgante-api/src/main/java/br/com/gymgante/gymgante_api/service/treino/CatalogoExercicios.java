package br.com.gymgante.gymgante_api.service.treino;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static br.com.gymgante.gymgante_api.service.treino.GrupoMuscular.*;
import static br.com.gymgante.gymgante_api.service.treino.Nivel.*;
import static br.com.gymgante.gymgante_api.service.treino.TipoExercicio.COMPOSTO;
import static br.com.gymgante.gymgante_api.service.treino.TipoExercicio.ISOLADO;

/** Todos os exercícios que o gerador por regras pode escolher. */
public final class CatalogoExercicios {

    private static final List<ExercicioCatalogo> TODOS = montar();

    private CatalogoExercicios() {
    }

    public static List<ExercicioCatalogo> todos() {
        return TODOS;
    }

    public static List<ExercicioCatalogo> doGrupo(GrupoMuscular grupo) {
        return TODOS.stream().filter(e -> e.grupo() == grupo).toList();
    }

    public static Optional<ExercicioCatalogo> porNome(String nome) {
        return TODOS.stream().filter(e -> e.nome().equals(nome)).findFirst();
    }

    private static List<ExercicioCatalogo> montar() {
        List<ExercicioCatalogo> l = new ArrayList<>();

        // ---------- PEITO ----------
        add(l, PEITO, COMPOSTO, INICIANTE, "Supino reto com barra", "Desça a barra até a linha do peito, com escápulas retraídas");
        add(l, PEITO, COMPOSTO, INICIANTE, "Supino reto com halteres", "Desça os halteres de forma controlada até a altura do peito");
        add(l, PEITO, COMPOSTO, INICIANTE, "Supino inclinado com halteres", "Banco entre 30° e 45°; não deixe os ombros subirem");
        add(l, PEITO, COMPOSTO, INICIANTE, "Flexão de braço", "Corpo alinhado da cabeça aos pés; desça até quase encostar o peito");
        add(l, PEITO, COMPOSTO, INTERMEDIARIO, "Supino declinado", "Pés bem presos; desça a barra na parte baixa do peito");
        add(l, PEITO, COMPOSTO, AVANCADO, "Mergulho nas paralelas (peito)", "Incline o tronco à frente; desça até 90° de cotovelo");
        add(l, PEITO, ISOLADO, INICIANTE, "Crucifixo com halteres", "Cotovelos levemente flexionados durante todo o movimento");
        add(l, PEITO, ISOLADO, INICIANTE, "Peck deck", "Junte as mãos à frente do peito sem bater os pesos");
        add(l, PEITO, ISOLADO, INTERMEDIARIO, "Crossover na polia", "Cruze as mãos à frente do corpo e segure 1 s na contração");
        add(l, PEITO, ISOLADO, INTERMEDIARIO, "Crucifixo inclinado", "Abra até sentir alongar o peito, sem passar da linha do ombro");

        // ---------- COSTAS ----------
        add(l, COSTAS, COMPOSTO, INICIANTE, "Puxada frontal", "Puxe a barra até a parte alta do peito, levando os cotovelos para baixo");
        add(l, COSTAS, COMPOSTO, INICIANTE, "Remada baixa sentada", "Coluna neutra; puxe com os cotovelos rentes ao corpo");
        add(l, COSTAS, COMPOSTO, INICIANTE, "Remada unilateral com halter", "Apoie joelho e mão no banco; puxe o halter em direção ao quadril");
        add(l, COSTAS, COMPOSTO, INTERMEDIARIO, "Remada curvada com barra", "Tronco inclinado a ~45°, coluna neutra, barra até o umbigo");
        add(l, COSTAS, COMPOSTO, AVANCADO, "Barra fixa", "Pegada pronada; suba até o queixo passar a barra");
        add(l, COSTAS, COMPOSTO, AVANCADO, "Levantamento terra", "Barra próxima às pernas, coluna neutra, empurre o chão com os pés");
        add(l, COSTAS, ISOLADO, INTERMEDIARIO, "Pulldown com braços estendidos", "Braços quase retos; leve a barra até as coxas contraindo as costas");
        add(l, COSTAS, ISOLADO, INTERMEDIARIO, "Pullover com halter", "Movimento em arco, sem dobrar muito os cotovelos");

        // ---------- OMBROS ----------
        add(l, OMBROS, COMPOSTO, INICIANTE, "Desenvolvimento com halteres", "Empurre os halteres acima da cabeça sem arquear a lombar");
        add(l, OMBROS, COMPOSTO, INTERMEDIARIO, "Desenvolvimento militar com barra", "Contraia abdômen e glúteos; barra passa rente ao rosto");
        add(l, OMBROS, COMPOSTO, AVANCADO, "Arnold press", "Gire as palmas durante a subida, de frente para fora");
        add(l, OMBROS, ISOLADO, INICIANTE, "Elevação lateral", "Suba os braços até a linha dos ombros, cotovelos levemente flexionados");
        add(l, OMBROS, ISOLADO, INICIANTE, "Elevação frontal", "Suba até a altura dos olhos, sem balançar o tronco");
        add(l, OMBROS, ISOLADO, INICIANTE, "Crucifixo inverso", "Tronco inclinado; abra os braços focando a parte de trás do ombro");
        add(l, OMBROS, ISOLADO, INTERMEDIARIO, "Elevação lateral na polia", "Cabo cruzando à frente do corpo; suba de forma controlada");

        // ---------- TRAPÉZIO ----------
        add(l, TRAPEZIO, ISOLADO, INICIANTE, "Encolhimento com halteres", "Suba os ombros em direção às orelhas, sem girar");
        add(l, TRAPEZIO, ISOLADO, INTERMEDIARIO, "Encolhimento com barra", "Segure 1 s em cima antes de descer");
        add(l, TRAPEZIO, COMPOSTO, INTERMEDIARIO, "Remada alta", "Puxe até a altura do peito, cotovelos acima das mãos");

        // ---------- BÍCEPS ----------
        add(l, BICEPS, ISOLADO, INICIANTE, "Rosca direta com barra", "Cotovelos fixos ao lado do corpo, sem balançar");
        add(l, BICEPS, ISOLADO, INICIANTE, "Rosca alternada com halteres", "Gire o punho durante a subida");
        add(l, BICEPS, ISOLADO, INICIANTE, "Rosca martelo", "Pegada neutra (palmas viradas uma para a outra)");
        add(l, BICEPS, ISOLADO, INICIANTE, "Rosca na polia", "Mantenha a tensão do cabo também na descida");
        add(l, BICEPS, ISOLADO, INTERMEDIARIO, "Rosca Scott", "Não estenda totalmente o cotovelo no final da descida");
        add(l, BICEPS, ISOLADO, INTERMEDIARIO, "Rosca concentrada", "Cotovelo apoiado na coxa; suba devagar");

        // ---------- TRÍCEPS ----------
        add(l, TRICEPS, ISOLADO, INICIANTE, "Tríceps na polia com corda", "Cotovelos fixos; abra a corda no final do movimento");
        add(l, TRICEPS, ISOLADO, INICIANTE, "Tríceps coice com halter", "Braço paralelo ao chão; estenda só o antebraço");
        add(l, TRICEPS, COMPOSTO, INICIANTE, "Mergulho no banco", "Mãos no banco, desça até 90° de cotovelo");
        add(l, TRICEPS, ISOLADO, INTERMEDIARIO, "Tríceps testa", "Desça a barra em direção à testa com os cotovelos parados");
        add(l, TRICEPS, ISOLADO, INTERMEDIARIO, "Tríceps francês", "Halter atrás da cabeça, cotovelos apontando para cima");
        add(l, TRICEPS, COMPOSTO, AVANCADO, "Supino fechado", "Mãos na largura dos ombros, cotovelos rentes ao corpo");

        // ---------- QUADRÍCEPS ----------
        add(l, QUADRICEPS, COMPOSTO, INICIANTE, "Leg press 45°", "Não deixe o quadril sair do banco na descida");
        add(l, QUADRICEPS, COMPOSTO, INICIANTE, "Agachamento livre", "Joelhos na direção dos pés, coluna neutra, desça até onde mantiver a postura");
        add(l, QUADRICEPS, COMPOSTO, INICIANTE, "Hack machine", "Pés na largura do quadril; desça de forma controlada");
        add(l, QUADRICEPS, COMPOSTO, INICIANTE, "Afundo", "Passo largo; joelho de trás quase encosta no chão");
        add(l, QUADRICEPS, COMPOSTO, INTERMEDIARIO, "Agachamento búlgaro", "Pé de trás apoiado no banco; tronco levemente inclinado");
        add(l, QUADRICEPS, COMPOSTO, AVANCADO, "Agachamento frontal", "Barra apoiada na frente dos ombros, cotovelos altos");
        add(l, QUADRICEPS, ISOLADO, INICIANTE, "Cadeira extensora", "Segure 1 s com a perna estendida");

        // ---------- POSTERIOR DE COXA ----------
        add(l, POSTERIOR, COMPOSTO, INICIANTE, "Stiff com halteres", "Joelhos levemente flexionados; desça com a coluna reta");
        add(l, POSTERIOR, COMPOSTO, INTERMEDIARIO, "Levantamento terra romeno", "Empurre o quadril para trás mantendo a barra rente às pernas");
        add(l, POSTERIOR, COMPOSTO, AVANCADO, "Good morning", "Barra nas costas; incline o tronco empurrando o quadril para trás");
        add(l, POSTERIOR, ISOLADO, INICIANTE, "Mesa flexora", "Não tire o quadril do banco durante a flexão");
        add(l, POSTERIOR, ISOLADO, INICIANTE, "Cadeira flexora", "Desça devagar, controlando o peso");
        add(l, POSTERIOR, ISOLADO, INTERMEDIARIO, "Flexora unilateral em pé", "Uma perna por vez; não gire o quadril");

        // ---------- GLÚTEOS ----------
        add(l, GLUTEOS, COMPOSTO, INICIANTE, "Elevação pélvica", "Suba o quadril contraindo os glúteos e segure 1 s");
        add(l, GLUTEOS, COMPOSTO, INICIANTE, "Agachamento sumô", "Pés afastados e apontados para fora");
        add(l, GLUTEOS, COMPOSTO, INICIANTE, "Passada", "Passos longos, tronco ereto");
        add(l, GLUTEOS, COMPOSTO, INICIANTE, "Step-up no banco", "Empurre com o calcanhar da perna de cima");
        add(l, GLUTEOS, ISOLADO, INICIANTE, "Abdução na máquina", "Abra as pernas de forma controlada, sem impulso");
        add(l, GLUTEOS, ISOLADO, INTERMEDIARIO, "Glúteo na polia", "Leve a perna para trás sem arquear a lombar");

        // ---------- PANTURRILHA ----------
        add(l, PANTURRILHA, ISOLADO, INICIANTE, "Panturrilha em pé", "Amplitude total: desça o calcanhar e suba na ponta dos pés");
        add(l, PANTURRILHA, ISOLADO, INICIANTE, "Panturrilha sentado", "Segure 1 s no topo");
        add(l, PANTURRILHA, ISOLADO, INICIANTE, "Panturrilha no leg press", "Só os tornozelos se movem");
        add(l, PANTURRILHA, ISOLADO, INTERMEDIARIO, "Panturrilha unilateral", "Uma perna por vez, com apoio para equilíbrio");

        // ---------- CORE ----------
        l.add(new ExercicioCatalogo("Prancha", CORE, ISOLADO, INICIANTE, "Corpo alinhado, abdômen contraído", "30-45 s"));
        l.add(new ExercicioCatalogo("Prancha lateral", CORE, ISOLADO, INICIANTE, "Quadril alto, corpo em linha reta", "20-30 s cada lado"));
        add(l, CORE, ISOLADO, INICIANTE, "Abdominal crunch", "Suba só até tirar as escápulas do chão");
        add(l, CORE, ISOLADO, INTERMEDIARIO, "Elevação de pernas", "Lombar colada no chão");
        add(l, CORE, ISOLADO, INTERMEDIARIO, "Abdominal na polia", "Flexione o tronco, não puxe com os braços");
        add(l, CORE, ISOLADO, AVANCADO, "Roda abdominal", "Avance só até onde mantiver a lombar neutra");

        return List.copyOf(l);
    }

    private static void add(List<ExercicioCatalogo> l, GrupoMuscular grupo, TipoExercicio tipo, Nivel nivel,
                            String nome, String dica) {
        l.add(new ExercicioCatalogo(nome, grupo, tipo, nivel, dica, null));
    }
}
