package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameApp extends JPanel {
    private int x = 200; // Posição inicial do personagem no eixo x
    private int y = 350; // Posição inicial do personagem no eixo y
    private final int MOVEMENT_SPEED = 20; // Velocidade de movimento do personagem
    private final int CHARACTER_WIDTH = 100; // Largura do sprite aumentada
    private final int CHARACTER_HEIGHT = 100; // Altura do sprite aumentada
    private Image[] rightSprites; // Sprites para movimento à direita
    private Image[] leftSprites; // Sprites para movimento à esquerda
    private Image[] stopSprites; // Sprites para animação de parada
    private Image[] explodeSprites; // Sprites para explosão
    private Image bombSprite, heartSprite, endSprite; // Sprites adicionais
    private int currentSpriteIndex = 0; // Índice atual do sprite de movimento
    private boolean isMoving = false; // Indica se o personagem está em movimento
    private Timer stopAnimationTimer; // Timer para alternar sprites quando parado
    private int stopSpriteIndex = 0; // Índice para os sprites de parada
    private String direction = "right"; // Direção inicial do personagem
    private List<Point> bombs; // Lista de bombas
    private Random random;
    private int lives = 3; // Quantidade inicial de vidas
    private boolean isGameOver = false; // Flag para indicar o fim do jogo
    private JButton restartButton; // Botão de reinício

    // Variáveis para controlar a explosão
    private boolean isExploding = false;
    private int explodeIndex = 0;
    private Timer explodeTimer;

    public GameApp() {
        // Carrega os sprites de movimento para a direita (de 2 a 9)
        rightSprites = new Image[8];
        for (int i = 2; i <= 9; i++) {
            try {
                rightSprites[i - 2] = ImageIO.read(new File("sprites/" + i + ".png"));
            } catch (IOException e) {
                System.out.println("Erro ao carregar a imagem do personagem: sprites/" + i + ".png");
                e.printStackTrace();
            }
        }

        // Carrega os sprites de movimento para a esquerda (de 2 a 9)
        leftSprites = new Image[8];
        for (int i = 2; i <= 9; i++) {
            try {
                leftSprites[i - 2] = ImageIO.read(new File("sprites/esq" + i + ".png"));
            } catch (IOException e) {
                System.out.println("Erro ao carregar a imagem do personagem: sprites/esq" + i + ".png");
                e.printStackTrace();
            }
        }

        // Carrega os sprites de parada (de stop1 a stop7)
        stopSprites = new Image[6];
        for (int i = 1; i <= 6; i++) {
            try {
                stopSprites[i - 1] = ImageIO.read(new File("sprites/stop" + i + ".png"));
            } catch (IOException e) {
                System.out.println("Erro ao carregar a imagem do personagem: sprites/stop" + i + ".png");
                e.printStackTrace();
            }
        }

        // Carrega os sprites de explosão (explode0 a explode6)
        explodeSprites = new Image[7];
        for (int i = 0; i <= 6; i++) {
            try {
                explodeSprites[i] = ImageIO.read(new File("sprites/explode" + i + ".png"));
            } catch (IOException e) {
                System.out.println("Erro ao carregar a imagem do personagem: sprites/explode" + i + ".png");
                e.printStackTrace();
            }
        }

        // Carrega os sprites adicionais (bomb, heart e end)
        try {
            bombSprite = ImageIO.read(new File("sprites/bomb.png"));
            heartSprite = ImageIO.read(new File("sprites/heart.png"));
            endSprite = ImageIO.read(new File("sprites/end.png"));
        } catch (IOException e) {
            System.out.println("Erro ao carregar a imagem adicional.");
            e.printStackTrace();
        }

        // Configura o Timer para alternar entre os sprites de parada
        stopAnimationTimer = new Timer(100, e -> {
            if (!isMoving && !isExploding && !isGameOver) {
                stopSpriteIndex = (stopSpriteIndex + 1) % stopSprites.length;
                repaint();
            }
        });
        stopAnimationTimer.start(); // Inicia o timer para a animação de parada

        // Configura o KeyListener para capturar as teclas de movimento
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!isGameOver && !isExploding) {
                    isMoving = true; // Indica que o personagem está em movimento

                    // Verifica a tecla pressionada e ajusta a posição e direção do personagem
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_UP -> {
                            if (y - MOVEMENT_SPEED >= 0) // Limite superior
                                y -= MOVEMENT_SPEED;
                        }
                        case KeyEvent.VK_DOWN -> {
                            if (y + MOVEMENT_SPEED + CHARACTER_HEIGHT <= getHeight()) // Limite inferior
                                y += MOVEMENT_SPEED;
                        }
                        case KeyEvent.VK_LEFT -> {
                            if (x - MOVEMENT_SPEED >= 0) { // Limite esquerdo
                                x -= MOVEMENT_SPEED;
                                direction = "left"; // Atualiza direção para esquerda
                            }
                        }
                        case KeyEvent.VK_RIGHT -> {
                            if (x + MOVEMENT_SPEED + CHARACTER_WIDTH <= getWidth()) { // Limite direito
                                x += MOVEMENT_SPEED;
                                direction = "right"; // Atualiza direção para direita
                            }
                        }
                    }

                    // Muda o índice do sprite atual para o próximo na sequência
                    currentSpriteIndex = (currentSpriteIndex + 1) % rightSprites.length;
                    repaint(); // Atualiza a tela para mostrar a nova posição e sprite do personagem
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                isMoving = false; // Personagem parou de se mover
                stopSpriteIndex = 0; // Reinicia a animação de parada do início
            }
        });

        // Configura a lista de bombas e o gerador aleatório
        bombs = new ArrayList<>();
        random = new Random();
        Timer bombTimer = new Timer(1000, e -> spawnBomb());
        bombTimer.start(); // Inicia o timer para criar bombas periodicamente

        // Configura o botão de reinício
        restartButton = new JButton("Reiniciar");
        restartButton.setFont(new Font("Arial", Font.BOLD, 20));
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGame();
            }
        });
        restartButton.setVisible(false); // Esconde o botão até o jogo acabar
        add(restartButton);

        setFocusable(true); // Necessário para que o painel receba eventos de teclado
    }

    private void spawnBomb() {
        if (!isGameOver) {
            int bombX = random.nextInt(getWidth() - 50);
            bombs.add(new Point(bombX, 0));
        }
    }

    private void checkCollisions() {
        List<Point> toRemove = new ArrayList<>();
        for (Point bomb : bombs) {
            if (new Rectangle(x, y, CHARACTER_WIDTH, CHARACTER_HEIGHT).intersects(new Rectangle(bomb.x, bomb.y, 50, 50))) {
                toRemove.add(bomb);
                triggerExplosion();
                break;
            }
        }
        bombs.removeAll(toRemove);
    }

    private void triggerExplosion() {
        isExploding = true;
        explodeIndex = 0;
        loseLife();

        explodeTimer = new Timer(100, e -> {
            if (explodeIndex < explodeSprites.length - 1) {
                explodeIndex++;
            } else {
                explodeTimer.stop();
                isExploding = false;
            }
            repaint();
        });
        explodeTimer.start();
    }

    private void loseLife() {
        lives--;
        if (lives <= 0) {
            isGameOver = true;
            restartButton.setVisible(true); // Mostra o botão de reinício
            repaint();
        }
    }

    private void resetGame() {
        lives = 3;
        isGameOver = false;
        bombs.clear();
        restartButton.setVisible(false);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Desenha o fim do jogo se todas as vidas acabarem
        if (isGameOver) {
            g.drawImage(endSprite, 0, 0, getWidth(), getHeight(), this);
            restartButton.setBounds(getWidth() / 2 - 75, getHeight() - 80, 150, 40);
            return;
        }

        // Desenha o personagem com a animação de explosão, se estiver explodindo
        if (isExploding && explodeSprites[explodeIndex] != null) {
            g.drawImage(explodeSprites[explodeIndex], x, y, CHARACTER_WIDTH, CHARACTER_HEIGHT, this);
        } else if (isMoving) {
            if (direction.equals("right") && rightSprites[currentSpriteIndex] != null) {
                g.drawImage(rightSprites[currentSpriteIndex], x, y, CHARACTER_WIDTH, CHARACTER_HEIGHT, this);
            } else if (direction.equals("left") && leftSprites[currentSpriteIndex] != null) {
                g.drawImage(leftSprites[currentSpriteIndex], x, y, CHARACTER_WIDTH, CHARACTER_HEIGHT, this);
            }
        } else {
            if (stopSprites[stopSpriteIndex] != null) {
                g.drawImage(stopSprites[stopSpriteIndex], x, y, CHARACTER_WIDTH, CHARACTER_HEIGHT, this);
            }
        }

        // Desenha as bombas e as movimenta
        List<Point> toRemove = new ArrayList<>();
        for (Point bomb : bombs) {
            g.drawImage(bombSprite, bomb.x, bomb.y, 50, 50, this);
            bomb.y += 5;
            if (bomb.y > getHeight()) toRemove.add(bomb); // Remove bombas que saem da tela
        }
        bombs.removeAll(toRemove);

        // Verifica colisões com bombas
        checkCollisions();

        // Desenha as vidas (hearts) no canto superior esquerdo
        for (int i = 0; i < lives; i++) {
            g.drawImage(heartSprite, 10 + i * 60, 10, 50, 50, this);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Jogo com Vidas e Reinício");
        GameApp gamePanel = new GameApp();
        frame.add(gamePanel);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
