package rocks.matrixgames.android;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jens on 17.12.16.
 */

public class Cell {

    private List<GameObject> gameObjects;

    public Cell() {
        this.gameObjects = new ArrayList<GameObject>();
    }

    public void addGameObject(GameObject go) {
        gameObjects.add(go);
    }

    public void removeGameObject(GameObject go) {
        gameObjects.remove(go);
    }

    public boolean hasGameObject(GameObject.KIND kind) {
        for (GameObject go : gameObjects) {
            if (go.getKind() == kind) return true;
        }
        return false;
    }

    public List<GameObject> getGameObjects() {
        return this.gameObjects;
    }


}
