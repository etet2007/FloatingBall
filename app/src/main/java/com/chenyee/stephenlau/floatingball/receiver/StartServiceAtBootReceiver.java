package com.chenyee.stephenlau.floatingball.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.chenyee.stephenlau.floatingball.floatBall.FloatingBallService;

import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.EXTRA_TYPE;

/**
 * Created by stephenlau on 18-3-14.
 */

public class StartServiceAtBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startServiceIntent = new Intent(context, FloatingBallService.class);
        Bundle data = new Bundle();
        data.putInt(EXTRA_TYPE, FloatingBallService.TYPE_ADD);
        startServiceIntent.putExtras(data);
    }
}
