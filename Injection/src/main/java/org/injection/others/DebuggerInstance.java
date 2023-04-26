package org.injection.others;

import com.sun.jdi.*;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.event.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.StepRequest;
import org.tools.Log;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

public class DebuggerInstance {
    private static final Log logger = Log.getInstance(DebuggerInstance.class);
    VirtualMachine vm;
    private Class debugClass;
    private int[] breakPointLines;

    public DebuggerInstance(Class debugClass, int[] breakPointLines) {
        this.debugClass = debugClass;
        this.breakPointLines = breakPointLines;
    }

    public VirtualMachine connectAndLaunchVM() throws Exception {
        LaunchingConnector launchingConnector = Bootstrap.virtualMachineManager()
                .defaultConnector();
        Map<String,Connector.Argument> arguments = launchingConnector.defaultArguments();
        arguments
                .get("main")
                .setValue(debugClass.getName());
        return launchingConnector.launch(arguments);
    }

    public void enableClassPrepareRequest(VirtualMachine vm) {
        ClassPrepareRequest classPrepareRequest = vm.eventRequestManager().createClassPrepareRequest();
        //classPrepareRequest.addClassFilter(debugClass.getName());
        classPrepareRequest.enable();
    }

    public void enableMethodEntryRequest(VirtualMachine vm) {
        MethodEntryRequest methodEntryRequest = vm.eventRequestManager().createMethodEntryRequest();
        //methodEntryRequest.addClassFilter(debugClass.getName());
        methodEntryRequest.enable();
    }

    public void setBreakPoints(VirtualMachine vm, ClassPrepareEvent event) throws AbsentInformationException {
        ClassType classType = (ClassType) event.referenceType();
        for(int lineNumber: breakPointLines) {
            Location location = classType.locationsOfLine(lineNumber).get(0);
            BreakpointRequest bpReq = vm
                    .eventRequestManager()
                    .createBreakpointRequest(location);
            bpReq.enable();
        }
    }

    public void displayVariables(LocatableEvent event) throws IncompatibleThreadStateException,
            AbsentInformationException {
        StackFrame stackFrame = event.thread().frame(0);
        if(stackFrame.location().toString().contains(debugClass.getName())) {
            Map<LocalVariable, Value> visibleVariables = stackFrame
                    .getValues(stackFrame.visibleVariables());
            logger.debug("Variables at " + stackFrame.location().toString() +  " > ");
            for (Map.Entry<LocalVariable, Value> entry : visibleVariables.entrySet()) {
                logger.debug(entry.getKey().name() + " = " + entry.getValue());
            }
        }
    }

    public void enableStepRequest(VirtualMachine vm, BreakpointEvent event) {
        // enable step request for last break point
        if (event.location().toString().
                contains(debugClass.getName() + ":" + breakPointLines[breakPointLines.length-1])) {
            StepRequest stepRequest = vm.eventRequestManager()
                    .createStepRequest(event.thread(), StepRequest.STEP_LINE, StepRequest.STEP_OVER);
            stepRequest.enable();
        }
    }

    public void start(PrintStream printStream) throws IOException {
        try {
            vm = connectAndLaunchVM();
            enableClassPrepareRequest(vm);
            List<ReferenceType> referenceTypes = vm.allClasses();
            for(ReferenceType referenceType : vm.allClasses()){
                logger.debug("referenceType: "+ referenceType.name()+" / "+ referenceType.instances(Long.MAX_VALUE).size());
            }
            //enableMethodEntryRequest(vm);
            EventSet eventSet;
            while ((eventSet = vm.eventQueue().remove()) != null) {
                for (com.sun.jdi.event.Event event : eventSet) {
                    try {
                        //logger.debug(event.request().getClass().getSimpleName());
                        /*if (event instanceof ClassPrepareEvent) {
                            ClassPrepareEvent classPrepareEvent = (ClassPrepareEvent) event;
                            ReferenceType referenceType = classPrepareEvent.referenceType();
                            logger.debug("ClassPrepareEvent : "+referenceType.name()+" , "+referenceType.instances(10).size());
                            //setBreakPoints(vm, (ClassPrepareEvent)event);
                        }
                        if (event instanceof BreakpointEvent) {
                            logger.debug("BreakpointEvent : ");
                            displayVariables((BreakpointEvent) event);
                            enableStepRequest(vm, (BreakpointEvent)event);
                        }
                        */
                        if (event instanceof StepEvent) {
                            StepEvent stepEvent = (StepEvent) event;
                            Location location = stepEvent.location();
                            logger.debug("StepEvent: "+location.sourcePath());
                            displayVariables(stepEvent);
                        }
                        /*
                        if (event instanceof MethodEntryEvent) {
                            MethodEntryEvent methodEntryEvent= (MethodEntryEvent) event;
                            Method method = methodEntryEvent.method();
                            ReferenceType declaringType = method.declaringType();
                            logger.debug("MethodEntryEvent: "+declaringType.name()+", "+declaringType.instances(0).size());
                        }*/
                    }catch (Exception e){
                        //e.printStackTrace();
                    }
                }
                vm.resume();
            }
        } catch (VMDisconnectedException e) {
            e.printStackTrace();
            logger.debug("Virtual Machine is disconnected.");
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            /*if(vm!=null && printStream!=null){
                InputStreamReader reader = new InputStreamReader(vm.process().getInputStream());
                OutputStreamWriter out = new OutputStreamWriter(printStream);
                char[] buf = new char[512];
                reader.read(buf);
                out.write(buf);
                out.flush();
            }*/
        }
    }

    public Class getDebugClass() {
        return debugClass;
    }

    public void setDebugClass(Class debugClass) {
        this.debugClass = debugClass;
    }

    public int[] getBreakPointLines() {
        return breakPointLines;
    }

    public void setBreakPointLines(int[] breakPointLines) {
        this.breakPointLines = breakPointLines;
    }
}
