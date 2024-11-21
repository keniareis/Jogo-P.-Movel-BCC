package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;

public class GameApp extends JPanel {
    private int x = 200; // Posição inicial do personagem no eixo x
    private int y = 350; // Posição inicial do personagem no eixo y
    private final int MOVEMENT_SPEED = 5; // Velocidade de movimento
    private final int CHARACTER_WIDTH = 50; // Largura do personagem
    private final int CHARACTER_HEIGHT = 100; // Altura do personagem

    private Image[] rightSprites;
    private Image[] leftSprites;
    private Image[] stopSprites;
    private Image[] explodeSprites;
    private Image bombSprite, heartSprite, endSprite, background;

    private int currentSpriteIndex = 0;
    private boolean isMoving = false; // Se o personagem está se movendo
    private boolean isExploding = false;
    private String direction = "stop"; // Inicialmente parado

    private List<Point> bombs = new ArrayList<>();
    private Random random = new Random();

    private int lives = 3;
    private boolean isGameOver = false;
    private Timer animationTimer;
    private Timer bombTimer;
    private Timer spawnBombTimer;
    private Timer explosionTimer;

    private JButton restartButton;
    private int timeLeft = 40;
    private boolean timeUp = false; // Indica se o tempo terminou

    private String message; // Declara a variável para armazenar a mensagem

    public GameApp() {
        setFocusable(true);
        loadSprites();
        setupKeyListener();
        setupTimers();
        setupRestartButton();
    }

    private void loadSprites() {
        try {
            background = ImageIO.read(new File("C:\\Users\\grupo\\IdeaProjects\\Jogo-P.-Movel-BCC\\sprites\\background.jpeg"));

            rightSprites = loadSpriteSet("sprites/", 2, 9);
            leftSprites = loadSpriteSet("sprites/esq", 2, 9);
            stopSprites = loadSpriteSet("sprites/stop", 1, 6);
            explodeSprites = loadSpriteSet("sprites/explode", 0, 5);

            bombSprite = ImageIO.read(new File("sprites/bomb.png"));
            heartSprite = ImageIO.read(new File("sprites/heart.png"));
            endSprite = ImageIO.read(new File("sprites/end.png"));
        } catch (IOException e) {
            System.out.println("Erro ao carregar os sprites: " + e.getMessage());
        }
    }

    private Image[] loadSpriteSet(String basePath, int start, int end) {
        Image[] sprites = new Image[end - start + 1];
        for (int i = start; i <= end; i++) {
            try {
                sprites[i - start] = ImageIO.read(new File(basePath + i + ".png"));
            } catch (IOException e) {
                System.out.println("Erro ao carregar sprite: " + basePath + i + ".png");
            }
        }
        return sprites;
    }

    private void setupKeyListener() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (isGameOver || isExploding || timeUp) return;

                isMoving = true;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT -> {
                        direction = "left"; // Atualiza a direção para "left"
                    }
                    case KeyEvent.VK_RIGHT -> {
                        direction = "right"; // Atualiza a direção para "right"
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // Quando a tecla for solta, o personagem para de se mover.
                isMoving = false;
                direction = "stop"; // Fica parado
            }
        });
    }

    private void setupTimers() {
        animationTimer = new Timer(100, e -> {
            if (!isGameOver && isMoving) { // Atualiza o índice apenas se o personagem estiver se movendo.
                currentSpriteIndex = (currentSpriteIndex + 1) % (direction.equals("right") ? rightSprites.length : leftSprites.length);
                repaint();
            }
        });
        animationTimer.start();

        bombTimer = new Timer(50, e -> updateBombs());
        bombTimer.start();

        spawnBombTimer = new Timer(500, e -> spawnBomb()); // Aumentei a frequência para gerar mais bombas.
        spawnBombTimer.start();

        // Temporizador para controlar o tempo do jogo
        Timer gameTimer = new Timer(1000, e -> {
            if (timeLeft > 0 && !isGameOver) {
                timeLeft--;
            } else {
                timeUp = true; // O tempo acabou
                endGame("Você Venceu!");
                repaint();
            }
        });
        gameTimer.start();
    }

    private void startExplosion() {
        isExploding = true;
        currentSpriteIndex = 0;

        explosionTimer = new Timer(100, e -> {
            if (currentSpriteIndex < explodeSprites.length - 1) {
                currentSpriteIndex++;
            } else {
                explosionTimer.stop();
                isExploding = false;

                if (lives <= 0) {
                    isGameOver = true;
                    endGame("Você Perdeu!");
                }
            }
            repaint();
        });
        explosionTimer.start();
    }

    private void setupRestartButton() {
        restartButton = new JButton("Reiniciar");
        restartButton.setFont(new Font("Monospaced", Font.BOLD, 20));
        restartButton.setVisible(false);
        restartButton.addActionListener(e -> resetGame());
        add(restartButton);
    }

    private void spawnBomb() {
        if (!isGameOver && !timeUp) {
            bombs.add(new Point(random.nextInt(getWidth() - 50), 0)); // Adiciona bombas no topo.
            repaint();
        }
    }

    private void updateBombs() {
        List<Point> toRemove = new ArrayList<>();
        for (Point bomb : bombs) {
            bomb.y += 5; // Move as bombas para baixo.
            if (bomb.y > getHeight()) {
                toRemove.add(bomb);
            } else if (bomb.x < x + CHARACTER_WIDTH && bomb.x + 50 > x && bomb.y < y + CHARACTER_HEIGHT && bomb.y + 50 > y) {
                toRemove.add(bomb);
                lives--;
                startExplosion();
            }
        }
        bombs.removeAll(toRemove);
        if (lives <= 0) {
            isGameOver = true;
            endGame("Você Perdeu!");
        }
        repaint();
    }

    private void resetGame() {
        lives = 3;
        isGameOver = false;
        timeUp = false;
        timeLeft = 45;
        bombs.clear();
        restartButton.setVisible(false);
        repaint();
    }

    private void endGame(String message) {
        isGameOver = true;
        timeUp = true; // O tempo acabou
        restartButton.setVisible(true);
        repaint();

        // Verifica se o jogador perdeu por falta de vidas ou venceu por tempo
        if (lives <= 0) {
            message = "Você Perdeu!"; // Se o jogador perder todas as vidas
        } else if (timeLeft == 0 && lives > 0) {
            message = "Você Venceu!"; // Se o jogador conseguiu escapar do tempo com vidas
        }

        // Agora a mensagem será desenhada diretamente dentro de paintComponent
        this.message = message; // Armazena a mensagem para ser desenhada no paintComponent
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), null);

        if (!isGameOver && !timeUp) {
            for (Point bomb : bombs) {
                g.drawImage(bombSprite, bomb.x, bomb.y, 50, 50, null);
            }

            // Movimentação do personagem controlada pelo jogador
            if (direction.equals("right") && x < getWidth() - CHARACTER_WIDTH) {
                x += MOVEMENT_SPEED; // Movimenta para a direita
            } else if (direction.equals("left") && x > 0) {
                x -= MOVEMENT_SPEED; // Movimenta para a esquerda
            }

            // Se o personagem estiver parado, usa o sprite de "stop"
            Image spriteToDraw = (isExploding ? explodeSprites[currentSpriteIndex] : (direction.equals("stop") ? stopSprites[0] : (direction.equals("right") ? rightSprites[currentSpriteIndex] : leftSprites[currentSpriteIndex])));
            g.drawImage(spriteToDraw, x, y, CHARACTER_WIDTH, CHARACTER_HEIGHT, null);

            // Desenha as vidas do personagem.
            for (int i = 0; i < lives; i++) {
                g.drawImage(heartSprite, i * 50, 0, 50, 50, null);
            }

            // Desenha o tempo restante ao lado das vidas.
            g.setFont(new Font("Monospaced", Font.BOLD, 20));
            g.setColor(Color.WHITE);
            g.drawString("Tempo: " + timeLeft + "s", 150, 30);
        } else {
            // Se o jogo acabou, desenha a mensagem (vitória ou derrota)
            g.setColor(Color.WHITE); // Define a cor do texto
            g.setFont(new Font("Monospaced", Font.BOLD, 30));
            g.drawString(message, getWidth() / 2 - 100, getHeight() / 2); // Exibe a mensagem no centro
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Jogo");
        GameApp gameApp = new GameApp();
        frame.add(gameApp);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        gameApp.requestFocusInWindow();
    }
}
