package rocks.matrixgames.android;

import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import rocks.matrixgames.android.mtxRendererService.GroundTexture;
import rocks.matrixgames.android.mtxRendererService.WallTexture;

/**
 * Created by jens on 01.12.16.
 */

public class Field {

    private short width = 0;
    private short height = 0;
    private LinkedHashMap<Integer, GameObject> objMap = null;
    private List<List<Cell>> cells = null;

    private int groundId = -1;

    private GameObject.KIND ground = GameObject.KIND.groundEarth;
    private GameObject.KIND wall = GameObject.KIND.wallRedBricks;

    public Field() {
        objMap = new LinkedHashMap<Integer, GameObject>();
    }

    public short getWidth() {
        return width;
    }

    public short getHeight() {
        return height;
    }

    public void setSize(short width, short height) {
        this.width = width;
        this.height = height;
        clear();
    }

    public void setGroundTexture(GroundTexture texture) {
        //ground = objects.GetGround(texture);
        ground = GameObject.KIND.groundEarth;
    }

    public void SetWallTexture(WallTexture texture) {
        //wall = objects.GetWall(texture);
        wall = GameObject.KIND.wallRedBricks;
    }

    public void clear() {
        objMap.clear();

        cells = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            ArrayList cols = new ArrayList<List>();
            cells.add(cols);
            for (int y = 0; y < height; y++) {
                cols.add(new Cell());
            }
        }
        groundId = -1;
    }

    public GameObject getObject(int objId) {
        if (!objMap.containsKey(objId)) {
            return null;
        }
        return objMap.get(objId);
    }

    public void add(int objId, char symbol, int x, int y) {
        GameObject.KIND prefab = null;
        Log.d(App.LOG_TAG, String.format("add field x=%d y=%d: %s", x, y, symbol));
        switch (symbol) {
            case '#':
                prefab = wall;
                break;
            case '1':
                prefab = GameObject.KIND.player1;
                break;
            case 'b':
                prefab = GameObject.KIND.boxPrefab;
                break;
            case 't':
                prefab = GameObject.KIND.targetPrefab;
                break;
            case '-':
                prefab = ground;
                break;
            case '·':
                prefab = ground;
                break;
            case '+':
                prefab = GameObject.KIND.tilePrefab;
                break;
            case '.':
                prefab = GameObject.KIND.pacDotPrefab;
                break;
            case 'e':
                prefab = GameObject.KIND.exitOpen;
                break;
            case 'E':
                prefab = GameObject.KIND.exitClosed;
                break;
            case 'k':
                prefab = GameObject.KIND.key;
                break;
            default:
                Log.w(App.LOG_TAG, "unknown field element " + symbol);
        }

        if (prefab != null) {
            GameObject obj = new GameObject(prefab);
            obj.setX(x);
            obj.setY(y);
            objMap.put(objId, obj);

            cells.get(obj.getX()).get(obj.getY()).addGameObject(obj);
        }
    }

    public void remove(int objId) {
        GameObject obj = getObject(objId);
        if (obj == null) return;

        // GameObject an alter Zelle entfernen
        cells.get(obj.getX()).get(obj.getY()).removeGameObject(obj);

        objMap.remove(objId);
    }

    public void move(int objId, int fromX, int fromY, int toX, int toY) {
        GameObject obj = getObject(objId);
        if (obj == null) return;

        // GameObject an alter Zelle entfernen
        cells.get(obj.getX()).get(obj.getY()).removeGameObject(obj);

        // Neue Position im GameObject setzen
        obj.setX(toX);
        obj.setY(toY);

        // GameObject in neuer Zelle einfuegen
        cells.get(obj.getX()).get(obj.getY()).addGameObject(obj);
    }

    public void jump(int objId, int fromX, int fromY, int toX, int toY) {
        GameObject obj = objMap.get(objId);
        if (obj == null) return;
        // GameObject an alter Zelle entfernen
        cells.get(obj.getX()).get(obj.getY()).removeGameObject(obj);

        obj.setX(toX);
        obj.setY(toY);

        // GameObject in neuer Zelle einfuegen
        cells.get(obj.getX()).get(obj.getY()).addGameObject(obj);
    }

    public Cell getCell(int x, int y) {
        return cells.get(x).get(y);
    }
}
