package rocks.matrixgames.android;

/**
 * Created by jens on 01.12.16.
 */

public class GameObject {

    public enum KIND {

        player1, 
        groundGras,groundWood,groundRock,
        groundSand      ,
        groundLava      ,
        groundSnow      ,
        groundIce       ,
        groundEarth     ,
        groundMetal     ,
        groundMarble    ,
        groundPavement  ,
        groundConcrete  ,
        wallRedBricks   ,
        wallWhiteBricks ,
        boxPrefab       ,
        targetPrefab    ,
        tilePrefab      ,
        pacDotPrefab,
        exitOpen,
        exitClosed,
        key
    }

    private KIND kind;
    private int x;
    private int y;

    public GameObject(KIND kind) {
        this.kind = kind;
    }


    public GameObject(KIND kind, int x, int y) {
        this(kind);
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public KIND getKind() {
        return kind;
    }
}
