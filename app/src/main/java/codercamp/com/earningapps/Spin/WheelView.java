package codercamp.com.earningapps.Spin;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.List;
import java.util.jar.Attributes;

import codercamp.com.earningapps.R;

public class WheelView extends RelativeLayout implements PieView.PieRotateListener{
    private  int mBackgroundColor,mTextColor;
    private Drawable CenterImage,CursorImage;
    private  PieView pieView;
    private ImageView cursorView;
    private LuckyRoundItemSelectedListener itemSelectedListener;

    public void LuckyRoundItemSelectedListener(LuckyRoundItemSelectedListener listener){
        this.itemSelectedListener = listener;
    }

    public interface LuckyRoundItemSelectedListener{
        void LuckyRoundItemSelected(int index);
    }


    @Override
    public void rotateDone(int index) {
        if(itemSelectedListener != null){
            itemSelectedListener.LuckyRoundItemSelected(index);
        }

    }

    public WheelView(Context context) {
        super(context);
        init(context,null);
    }

    public WheelView(Context context, AttributeSet attributeSet){
        super(context,attributeSet);
        init(context,attributeSet);

    }
    private  void  init(Context context, AttributeSet attributeSet){
        if (attributeSet != null){

            TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.WheelView);
            mBackgroundColor = typedArray.getColor(R.styleable.WheelView_BackgroundColor,0xffcc0000 );
            mTextColor = typedArray.getColor(R.styleable.WheelView_TextColor,0xffffffff); // 8 f

            CursorImage = typedArray.getDrawable(R.styleable.WheelView_CursorImage);
            CenterImage = typedArray.getDrawable(R.styleable.WheelView_CenterImage);

            typedArray.recycle();
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        FrameLayout frameLayout = (FrameLayout) inflater.inflate(R.layout.wheel_layout,this,false);
        pieView = frameLayout.findViewById(R.id.pieView);
        cursorView = frameLayout.findViewById(R.id.cursorView);

        pieView.setPieRotateListener(this);
        pieView.setPieCenterImage(CenterImage);
        pieView.setPieBackgroundColor(mBackgroundColor);
        pieView.setPieTextColor(mTextColor);
        cursorView.setImageDrawable(CursorImage);
        addView(frameLayout);

    }

    public void setWheelBackgroundColor(int color){
        pieView.setPieBackgroundColor(color);
    }
    public void setWheelCursorImage(int drawable){
        cursorView.setBackgroundResource(drawable);

    }
    public void setWheelCenterImage(Drawable drawable){
        pieView.setPieCenterImage(drawable);
    }
    public void setWheelTextColor(int color){
        pieView.setPieTextColor(color);
    }
    public void setData(List<SpinModel> data){
        pieView.setData(data);
    }

    public  void  setRound(int numberOfRound){
        pieView.setRound(numberOfRound);
    }

    public void startWheelWithTargetIndex(int index){
        pieView.rotateTo(index);
    }
}
