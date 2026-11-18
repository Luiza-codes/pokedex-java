package com.pokedex;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ExecutionException;


public class PokedexFrame extends JFrame {

    private static final Color VERMELHO = new Color(0xD32F2F);
    private static final Color VERMELHO_ESCURO = new Color(0xA31515);
    private static final Color TELA = new Color(0xE8F5E9);

    private final PokeApiClient client = new PokeApiClient();

    private final JTextField campoBusca = new JTextField(14);
    private final JButton botaoBuscar = new JButton("Buscar");
    private final JButton botaoAnterior = new JButton("◀");
    private final JButton botaoProximo = new JButton("▶");

    private final JLabel imagem = new JLabel("", SwingConstants.CENTER);
    private final JLabel lblNome = new JLabel("Pokédex", SwingConstants.CENTER);
    private final JLabel lblTipos = new JLabel(" ");
    private final JLabel lblAltura = new JLabel(" ");
    private final JLabel lblPeso = new JLabel(" ");
    private final JLabel lblHabilidades = new JLabel(" ");
    private final JLabel lblStatus = new JLabel("Digite um nome ou número e clique em Buscar.");

    private int idAtual = 0;

    public PokedexFrame() {
        super("Pokédex");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 620);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel raiz = new JPanel(new BorderLayout(10, 10));
        raiz.setBackground(VERMELHO);
        raiz.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        raiz.add(criarPainelBusca(), BorderLayout.NORTH);
        raiz.add(criarPainelTela(), BorderLayout.CENTER);
        raiz.add(criarPainelRodape(), BorderLayout.SOUTH);
        setContentPane(raiz);

        // Enter também busca
        campoBusca.addActionListener(e -> buscar(campoBusca.getText()));
        botaoBuscar.addActionListener(e -> buscar(campoBusca.getText()));
        botaoAnterior.addActionListener(e -> buscar(String.valueOf(Math.max(1, idAtual - 1))));
        botaoProximo.addActionListener(e -> buscar(String.valueOf(idAtual + 1)));
        botaoAnterior.setEnabled(false);
        botaoProximo.setEnabled(false);
    }

    private JPanel criarPainelBusca() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        p.setOpaque(false);
        JLabel titulo = new JLabel("Buscar:");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 14f));
        p.add(titulo);
        p.add(campoBusca);
        p.add(botaoBuscar);
        return p;
    }

    private JPanel criarPainelTela() {
        JPanel tela = new JPanel();
        tela.setLayout(new BoxLayout(tela, BoxLayout.Y_AXIS));
        tela.setBackground(TELA);
        tela.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VERMELHO_ESCURO, 4, true),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));

        imagem.setPreferredSize(new Dimension(200, 200));
        imagem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        imagem.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblNome.setFont(lblNome.getFont().deriveFont(Font.BOLD, 24f));
        lblNome.setAlignmentX(Component.CENTER_ALIGNMENT);

        for (JLabel l : new JLabel[]{lblTipos, lblAltura, lblPeso, lblHabilidades}) {
            l.setFont(l.getFont().deriveFont(Font.PLAIN, 15f));
            l.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        tela.add(imagem);
        tela.add(Box.createVerticalStrut(8));
        tela.add(lblNome);
        tela.add(Box.createVerticalStrut(16));
        tela.add(lblTipos);
        tela.add(Box.createVerticalStrut(8));
        tela.add(lblAltura);
        tela.add(Box.createVerticalStrut(8));
        tela.add(lblPeso);
        tela.add(Box.createVerticalStrut(8));
        tela.add(lblHabilidades);
        return tela;
    }

    private JPanel criarPainelRodape() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);

        JPanel navegacao = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        navegacao.setOpaque(false);
        navegacao.add(botaoAnterior);
        navegacao.add(botaoProximo);

        lblStatus.setForeground(Color.WHITE);
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);

        p.add(navegacao, BorderLayout.NORTH);
        p.add(lblStatus, BorderLayout.SOUTH);
        return p;
    }


    private void buscar(String termo) {
        if (termo == null || termo.isBlank()) {
            lblStatus.setText("Digite um nome ou número.");
            return;
        }
        definirCarregando(true);
        lblStatus.setText("Buscando...");

        new SwingWorker<Resultado, Void>() {
            @Override
            protected Resultado doInBackground() throws Exception {
                Optional<Pokemon> encontrado = client.buscar(termo);
                if (encontrado.isEmpty()) {
                    return null;
                }
                Pokemon pokemon = encontrado.get();
                ImageIcon icone = null;
                if (pokemon.getSpriteUrl() != null) {
                    BufferedImage img = ImageIO.read(URI.create(pokemon.getSpriteUrl()).toURL());
                    // sprites são pequenos (96x96): ampliamos sem suavizar para manter o visual pixelado
                    icone = new ImageIcon(img.getScaledInstance(192, 192, Image.SCALE_FAST));
                }
                return new Resultado(pokemon, icone);
            }

            @Override
            protected void done() {
                definirCarregando(false);
                try {
                    Resultado r = get();
                    if (r == null) {
                        lblStatus.setText("Pokémon não encontrado: " + termo);
                    } else {
                        mostrar(r);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    lblStatus.setText("Erro de comunicação: " + e.getCause().getMessage());
                }
            }
        }.execute();
    }

    private void mostrar(Resultado r) {
        Pokemon p = r.pokemon();
        idAtual = p.getId();
        imagem.setIcon(r.icone());
        lblNome.setText("#" + p.getId() + "  " + capitalizar(p.getName()));
        lblTipos.setText("Tipo(s): " + p.getTypesAsText());
        lblAltura.setText(String.format("Altura: %.1f m", p.getHeightInMeters()));
        lblPeso.setText(String.format("Peso: %.1f kg", p.getWeightInKg()));
        lblHabilidades.setText("Habilidades: " + p.getAbilitiesAsText());
        lblStatus.setText("Pokémon carregado.");
        botaoAnterior.setEnabled(idAtual > 1);
        botaoProximo.setEnabled(true);
    }

    private void definirCarregando(boolean carregando) {
        botaoBuscar.setEnabled(!carregando);
        campoBusca.setEnabled(!carregando);
        if (carregando) {
            botaoAnterior.setEnabled(false);
            botaoProximo.setEnabled(false);
        }
    }

    private static String capitalizar(String s) {
        return s == null || s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private record Resultado(Pokemon pokemon, ImageIcon icone) { }
}
