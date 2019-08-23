package com.chenyee.stephenlau.floatingball.common_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.chenyee.stephenlau.floatingball.floatingBall.service.FloatingBallService;

import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.EXTRAS_COMMAND;

/**
 * Created by stephenlau on 18-3-14.
 */

public class StartServiceAtBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startServiceIntent = new Intent(context, FloatingBallService.class);
        Bundle data = new Bundle();
        data.putInt(EXTRAS_COMMAND, FloatingBallService.TYPE_START);
        startServiceIntent.putExtras(data);
    }
}
