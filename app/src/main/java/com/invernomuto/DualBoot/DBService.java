package com.invernomuto.DualBoot;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.controls.Control;
import android.service.controls.ControlsProviderService;
import android.service.controls.DeviceTypes;
import android.service.controls.actions.ControlAction;
import android.service.controls.templates.ControlButton;
import android.service.controls.templates.ToggleTemplate;

import androidx.annotation.RequiresApi;

import com.topjohnwu.superuser.Shell;

import org.reactivestreams.FlowAdapters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.function.Consumer;

import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.processors.ReplayProcessor;

/**
 * An {@link --IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
@RequiresApi(api = Build.VERSION_CODES.R)
public class DBService extends ControlsProviderService {

    private ReplayProcessor updatePublisher;
    //PendingIntent pi;
    String activeslot;
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public Flow.Publisher<Control> createPublisherForAllAvailable() {

        activeslot = getText(R.string.not_found).toString();
        List<String> out = new ArrayList<String>();
        out = Shell.su("getprop ro.boot.slot_suffix").exec().getOut();
        if (out.toString().contains("a")) activeslot = "A";
        else if (out.toString().contains("b")) activeslot = "B";

        Context context = getBaseContext();
        Intent i = new Intent();
        PendingIntent pi = PendingIntent.getActivity(context, 1, i, PendingIntent.FLAG_UPDATE_CURRENT);
        List controls = new ArrayList<>();
        Control control = new Control.StatelessBuilder("REBOOT-A", pi)
                // Required: The name of the control
                .setTitle(getText(R.string.reboot_to_system))
                // Required: Usually the room where the control is located
                .setSubtitle(getText(R.string.slota))
                .setCustomIcon(Icon.createWithResource(this, R.drawable.a))
                // Optional: Structure where the control is located, an example would be a house
                .setStructure(getString(R.string.DualBoot) + getString(R.string.active_slot) + activeslot)
                // Required: Type of device, i.e., thermostat, light, switch
                .setDeviceType(DeviceTypes.TYPE_GENERIC_ON_OFF) // For example, DeviceTypes.TYPE_THERMOSTAT
                .build();
        controls.add(control);

        control = new Control.StatelessBuilder("REBOOT-B", pi)
                // Required: The name of the control
                .setTitle(getText(R.string.reboot_to_system))
                // Required: Usually the room where the control is located
                .setSubtitle(getText(R.string.slotb))
                .setCustomIcon(Icon.createWithResource(this, R.drawable.b))
                // Optional: Structure where the control is located, an example would be a house
                .setStructure(getString(R.string.DualBoot) + getString(R.string.active_slot) + activeslot)
                // Required: Type of device, i.e., thermostat, light, switch
                .setDeviceType(DeviceTypes.TYPE_GENERIC_ON_OFF) // For example, DeviceTypes.TYPE_THERMOSTAT
                .build();
        controls.add(control);

        control = new Control.StatelessBuilder("REBOOT-RA", pi)
                // Required: The name of the control
                .setTitle(getText(R.string.reboot_to_recovery))
                // Required: Usually the room where the control is located
                .setSubtitle(getText(R.string.slota))
                .setCustomIcon(Icon.createWithResource(this, R.drawable.ra))
                // Optional: Structure where the control is located, an example would be a house
                .setStructure(getString(R.string.DualBoot) + getString(R.string.active_slot) + activeslot)
                // Required: Type of device, i.e., thermostat, light, switch
                .setDeviceType(DeviceTypes.TYPE_GENERIC_ON_OFF) // For example, DeviceTypes.TYPE_THERMOSTAT
                .build();
        controls.add(control);

        control = new Control.StatelessBuilder("REBOOT-RB", pi)
                // Required: The name of the control
                .setTitle(getText(R.string.reboot_to_recovery))
                // Required: Usually the room where the control is located
                .setSubtitle(getText(R.string.slotb))
                .setCustomIcon(Icon.createWithResource(this, R.drawable.rb))
                // Optional: Structure where the control is located, an example would be a house
                .setStructure(getString(R.string.DualBoot) + getString(R.string.active_slot) + activeslot)
                // Required: Type of device, i.e., thermostat, light, switch
                .setDeviceType(DeviceTypes.TYPE_GENERIC_ON_OFF) // For example, DeviceTypes.TYPE_THERMOSTAT
                .build();
        controls.add(control);
        // Create more controls here if needed and add it to the ArrayList

        // Uses the RxJava 2 library
        return FlowAdapters.toFlowPublisher(Flowable.fromIterable(controls));
    }

    @NonNull
    @Override
    public Flow.Publisher<Control> createPublisherFor(@NonNull List<String> controlIds) {

        activeslot = getText(R.string.not_found).toString();
        List<String> out = new ArrayList<String>();
        out = Shell.su("getprop ro.boot.slot_suffix").exec().getOut();
        if (out.toString().contains("a")) activeslot = "A";
        else if (out.toString().contains("b")) activeslot = "B";

        Context context = getBaseContext();
        /* Fill in details for the activity related to this device. On long press,
         * this Intent will be launched in a bottomsheet. Please design the activity
         * accordingly to fit a more limited space (about 2/3 screen height).
         */

        Intent i = new Intent();
        PendingIntent pi = PendingIntent.getActivity(context, 1, i, PendingIntent.FLAG_UPDATE_CURRENT);

        updatePublisher = ReplayProcessor.create();

        // For each controlId in controlIds


        if (controlIds.contains("REBOOT-A")) {

            Control control = new Control.StatefulBuilder("REBOOT-A", pi)
                    // Required: The name of the control
                    .setTitle(getText(R.string.reboot_to_system))
                    // Required: Usually the room where the control is located
                    .setSubtitle(getText(R.string.slota))
                    // Optional: Structure where the control is located, an example would be a house
                    .setStructure(getString(R.string.DualBoot) + getString(R.string.active_slot) + activeslot)
                    .setCustomIcon(Icon.createWithResource(this, R.drawable.a))
                    .setStatusText(getString(R.string.Ready))
                    .setControlTemplate(new ToggleTemplate("REBOOT-A", new ControlButton(true, "BUTTON-A")))
                    // Required: Type of device, i.e., thermostat, light, switch
                    .setDeviceType(DeviceTypes.TYPE_GENERIC_ON_OFF) // For example, DeviceTypes.TYPE_THERMOSTAT
                    // Required: Current status of the device
                    .setStatus(Control.STATUS_OK) // For example, Control.STATUS_OK
                    .build();

            updatePublisher.onNext(control);
        }
        if (controlIds.contains("REBOOT-B")) {

            Control control = new Control.StatefulBuilder("REBOOT-B", pi)
                    // Required: The name of the control
                    .setTitle(getText(R.string.reboot_to_system))
                    // Required: Usually the room where the control is located
                    .setSubtitle(getText(R.string.slotb))
                    // Optional: Structure where the control is located, an example would be a house
                    .setStructure(getString(R.string.DualBoot) + getString(R.string.active_slot) + activeslot)
                    .setCustomIcon(Icon.createWithResource(this, R.drawable.b))
                    .setStatusText(getString(R.string.Ready))
                    .setControlTemplate(new ToggleTemplate("REBOOT-B", new ControlButton(true, "BUTTON-B")))
                    // Required: Type of device, i.e., thermostat, light, switch
                    .setDeviceType(DeviceTypes.TYPE_GENERIC_ON_OFF) // For example, DeviceTypes.TYPE_THERMOSTAT
                    // Required: Current status of the device
                    .setStatus(Control.STATUS_OK) // For example, Control.STATUS_OK
                    .build();

            updatePublisher.onNext(control);
        }
        if (controlIds.contains("REBOOT-RA")) {

            Control control = new Control.StatefulBuilder("REBOOT-RA", pi)
                    // Required: The name of the control
                    .setTitle(getText(R.string.reboot_to_recovery))
                    // Required: Usually the room where the control is located
                    .setSubtitle(getText(R.string.slota))
                    .setCustomIcon(Icon.createWithResource(this, R.drawable.ra))
                    .setStatusText(getString(R.string.Ready))
                    .setControlTemplate(new ToggleTemplate("REBOOT-RA", new ControlButton(true, "BUTTON-RA")))
                    // Optional: Structure where the control is located, an example would be a house
                    .setStructure(getString(R.string.DualBoot) + getString(R.string.active_slot) + activeslot)
                    // Required: Type of device, i.e., thermostat, light, switch
                    .setDeviceType(DeviceTypes.TYPE_GENERIC_ON_OFF) // For example, DeviceTypes.TYPE_THERMOSTAT
                    // Required: Current status of the device
                    .setStatus(Control.STATUS_OK) // For example, Control.STATUS_OK
                    .build();

            updatePublisher.onNext(control);
        }
        if (controlIds.contains("REBOOT-RB")) {

            Control control = new Control.StatefulBuilder("REBOOT-RB", pi)
                    // Required: The name of the control
                    .setTitle(getText(R.string.reboot_to_recovery))
                    // Required: Usually the room where the control is located
                    .setSubtitle(getText(R.string.slotb))
                    // Optional: Structure where the control is located, an example would be a house
                    .setStructure(getString(R.string.DualBoot) + getString(R.string.active_slot) + activeslot)
                    .setCustomIcon(Icon.createWithResource(this, R.drawable.rb))
                    .setStatusText(getString(R.string.Ready))
                    .setControlTemplate(new ToggleTemplate("REBOOT-RB", new ControlButton(true, "BUTTON-RB")))
                    // Required: Type of device, i.e., thermostat, light, switch
                    .setDeviceType(DeviceTypes.TYPE_GENERIC_ON_OFF) // For example, DeviceTypes.TYPE_THERMOSTAT
                    // Required: Current status of the device
                    .setStatus(Control.STATUS_OK) // For example, Control.STATUS_OK
                    .build();

            updatePublisher.onNext(control);
        }
        // Uses the Reactive Streams API
        return FlowAdapters.toFlowPublisher(updatePublisher);
    }

    @Override
    public void performControlAction(@NonNull String controlId, @NonNull ControlAction action, @NonNull Consumer<Integer> consumer) {
        /* First, locate the control identified by the controlId. Once it is located, you can
         * interpret the action appropriately for that specific device. For instance, the following
         * assumes that the controlId is associated with a light, and the light can be turned on
         * or off.
         */
        List<String> out = new ArrayList<String>();

        if(controlId.contains("REBOOT-A"))
        {
            out = Shell.su("/data/adb/Dualboot/bootctl set-active-boot-slot 0").exec().getOut();
            out = Shell.su("reboot").exec().getOut();
        }
        else if (controlId.contains("REBOOT-B"))
        {
            out = Shell.su("/data/adb/Dualboot/bootctl set-active-boot-slot 1").exec().getOut();
            out = Shell.su("reboot").exec().getOut();
        }
        else if (controlId.contains("REBOOT-RA"))
        {
            out = Shell.su("/data/adb/Dualboot/bootctl set-active-boot-slot 0").exec().getOut();
            out = Shell.su("reboot recovery").exec().getOut();
        }
        else if (controlId.contains("REBOOT-RB"))
        {
            out = Shell.su("/data/adb/Dualboot/bootctl set-active-boot-slot 1").exec().getOut();
            out = Shell.su("reboot recovery").exec().getOut();
        }

        /*
        if (action instanceof BooleanAction) {
         */

            // Inform SystemUI that the action has been received and is being processed
            consumer.accept(ControlAction.RESPONSE_OK);

            /*Context context = getBaseContext();
            Intent i = new Intent();
            PendingIntent pi = PendingIntent.getActivity(context, 1, i, PendingIntent.FLAG_UPDATE_CURRENT);

            BooleanAction action_ = (BooleanAction) action;
            */

            // In this example, action.getNewState() will have the requested action: true for “On”,
            // false for “Off”.

            /* This is where application logic/network requests would be invoked to update the state of
             * the device.
             * After updating, the application should use the publisher to update SystemUI with the new
             * state.


            Control control = new Control.StatefulBuilder("REBOOT-RA", pi)
                    // Required: The name of the control
                    .setTitle("Reboot to slot A")
                    // Required: Usually the room where the control is located
                    .setSubtitle("System")
                    // Optional: Structure where the control is located, an example would be a house
                    .setStructure("DualBoot")
                    // Required: Type of device, i.e., thermostat, light, switch
                    .setDeviceType(DeviceTypes.TYPE_GENERIC_ON_OFF) // For example, DeviceTypes.TYPE_THERMOSTAT
                    // Required: Current status of the device
                    .setStatus(Control.STATUS_OK) // For example, Control.STATUS_OK
                    .build();

            // This is the publisher the application created during the call to createPublisherFor()
            updatePublisher.onNext(control);


        } */
    }
}