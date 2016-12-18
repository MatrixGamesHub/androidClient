package rocks.matrixgames.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by jens on 22.11.16.
 */

public class GameView extends View {

    private int numRows = 8;
    private int numCols = 8;

    private Paint paintLine;
    private Paint paintFill;

    private float pixelWidth;
    private float pixelHeight;
    private Field field;

    private boolean isPortrait = true;


    public GameView(Context context) {
        super(context);
        init(null, 0);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public GameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void setFieldSize(int width, int height) {
        numCols = width;
        numRows = height;
    }

    public void setField(Field field) {
        this.field = field;
    }

    private void init(AttributeSet attrs, int defStyle) {
        paintLine = new Paint();
        paintLine.setStrokeWidth(2);
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setColor(Color.argb(255, 0, 0, 0));

        paintFill = new Paint();
        paintFill.setStrokeWidth(2);
        paintFill.setStyle(Paint.Style.FILL);
        paintFill.setColor(Color.argb(255, 222, 222, 222));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (heightSize > widthSize) {
            // portrait
            isPortrait = true;
            width = widthSize;
            height = getPaddingTop();
            height += (widthSize / numCols) * numRows;
            height += getPaddingBottom();
        } else {
            // landscape
            isPortrait = false;
            height = heightSize;
            width = heightSize;
        }

        if (field != null) {
            pixelWidth = width / field.getWidth();
            pixelHeight = height / field.getHeight();
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth();// - paddingLeft - paddingRight;
        int contentHeight = getHeight();// - paddingTop - paddingBottom;

        float startX = paddingLeft;
        float startY = paddingTop;

        float cellSize;
        float endX;
        float endY;

        if (isPortrait) {
            // portrait
            endX = contentWidth - paddingRight;

            float width = endX - startX;

            cellSize = width / numCols;
            endY = startY + (cellSize * numRows);
        } else {
            // landscape
            endY = contentHeight - paddingBottom;

            float height = endY - startY;

            cellSize = height / numCols;
            endX = startX + (cellSize * numCols);
        }

        RectF outer = new RectF(startX - 5, startY - 5, endX + 5, endY + 5);

        canvas.drawRoundRect(outer, 10, 10, paintFill);
        canvas.drawRoundRect(outer, 10, 10, paintLine);

        if (field != null) {
            for (int x = 0; x < field.getWidth(); x++) {
                for (int y = 0; y < field.getHeight(); y++) {
                    drawCell(canvas, field.getCell(x, y));
                }
            }

        }
    }


    private void drawPixel(Canvas canvas, int x, int y, int color) {
        // Clear pixel first, to avoid relicts on the edges from antialiasing.
        //self._dcBmp.SetBrush(wx.Brush(colour=self._bgColor))
        //self._dcBmp.DrawRectangle(left, top, self._pixelWidth, self._pixelHeight);

        float left = x * pixelWidth;
        float top = y * pixelHeight;
        canvas.drawRect(left, top, left + pixelWidth, top + pixelHeight, paintLine);

        // Draw the Pixel
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);

        RectF cell = new RectF(left, top, (left + pixelWidth), (top + pixelHeight));
        canvas.drawRoundRect(cell, 2f, 2f, paint);
        // gc.DrawRoundedRectangle(point.x * self._pixelWidth, point.y * self._pixelHeight, self._pixelWidth - 2, self._pixelHeight - 2, 2)
    }

    private void drawCell(Canvas canvas, Cell cell) {
        char symbol = 'a';
        int color = Color.argb(255, 0, 0, 0);

        for (GameObject go : cell.getGameObjects()) {
            switch (go.getKind()) {
                case wallRedBricks: // '#':
                    color = Color.argb(255, 255, 0, 0);
                    break;
                case player1: // '1':
                    color = Color.argb(255, 255, 255, 255);
                    break;
                case boxPrefab: // 'b':
                    if (cell.hasGameObject(GameObject.KIND.targetPrefab)) {
                        color = Color.argb(255, 0, 255, 0); //green
                    } else {
                        color = Color.argb(255, 255, 255, 0); //yellow
                    }
                    break;
                case targetPrefab:
                    color = Color.argb(255, 120, 55, 0); // dark brown
                    break;
                case tilePrefab:
                    /*
                    int cnt = cell.getObjectCount('+');
                    if (cnt == 1) color = Color.argb(255, 0, 255, 0);
                    else if (cnt == 2) color = Color.argb(255, 0, 0, 255);
                    else*/
                    color = Color.argb(255, 255, 0, 0);
                    break;
                case pacDotPrefab:
                    color = Color.argb(255, 255, 255, 0);
                    break;
                case groundEarth:
                    color = Color.argb(255, 0, 0, 0);
                    break;
                case exitOpen:
                    color = Color.argb(255, 200, 255, 200);
                    break;
                case exitClosed:
                    color = Color.argb(255, 255, 200, 200);
                    break;
                case key:
                    color = Color.argb(255, 200, 200, 255);
                    break;
                default:
                    Log.w(App.LOG_TAG, "unknown object " + go.getKind());
            }
            drawPixel(canvas, go.getX(), go.getY(), color);
        }
    }

}
