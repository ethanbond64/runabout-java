package dev.runabout.agent;

import dev.runabout.RunaboutException;
import dev.runabout.RunaboutListener;
import dev.runabout.RunaboutService;
import dev.runabout.RunaboutUtils;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

class RunaboutAgentImpl implements RunaboutAgent {

    private static final ClassReloadingStrategy.Strategy STRATEGY = ClassReloadingStrategy.Strategy.RETRANSFORMATION;

    private final String serverGroup;
    private final ContextProvider contextProvider;
    private final RunaboutService runaboutService;
    private final RunaboutListener runaboutListener;

    private final AtomicBoolean installed = new AtomicBoolean(false);

    RunaboutAgentImpl(String serverGroup,
                      ContextProvider contextProvider,
                      RunaboutService runaboutService,
                      RunaboutListener runaboutListener) {
        this.serverGroup = serverGroup;
        this.contextProvider = contextProvider;
        this.runaboutService = runaboutService;
        this.runaboutListener = runaboutListener;
    }

    @Override
    public void install() {
        if (!installed.get()) {
            ByteBuddyAgent.install();
//            register("TODO", serverGroup); // TODO register via RunaboutAPI
        }
        MethodInterceptor.setRunaboutListener(runaboutListener);
        MethodInterceptor.setContextProvider(contextProvider);
        MethodInterceptor.setRunaboutService(runaboutService);
        MethodInterceptor.enable();
        installed.set(true);
    }

    @Override
    public void refresh() {
        // TODO callout to runabout API to get latest commands.
        final String json = "TODO";
        final List<Instruction> instructions = List.of(Instruction.of(json));
    }

    @Override
    public void refresh(Set<Instruction> instructions) {
        // TODO store the list and implement/restore based on the delta
        final InstructionStore instructionStore = InstructionStore.newCommandStore(instructions, runaboutListener);
        MethodInterceptor.setCommandStore(instructionStore); // TODO allow resetting this one?
    }

    @Override
    public void disable() {
        MethodInterceptor.disable();
        // TODO stop polling.
    }

    private void handleCommand(final Instruction instruction) throws RunaboutException {

        Objects.requireNonNull(instruction.getReference(), "Invalid command, missing reference.");

        final Pair<Class<?>, Method> references = getReferences(instruction.getReference());
        final Class<?> clazz = references.left;
        final Method method = references.right;

    }

    private static void loadInterceptor(final Class<?> clazz, final Method method) {
        final AsmVisitorWrapper visitor = Advice
                .to(MethodInterceptor.class)
                .on(method == null ? ElementMatchers.not(ElementMatchers.isConstructor()) : ElementMatchers.is(method));
        try (final DynamicType.Unloaded<?> unloaded = new ByteBuddy().redefine(clazz).visit(visitor).make()) {
            unloaded.load(clazz.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent(STRATEGY));
        }
    }

    private static void restore(final Class<?> clazz) {
        try (final DynamicType.Unloaded<?> unloaded = new ByteBuddy().redefine(clazz).make()) {
            unloaded.load(clazz.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent(STRATEGY));
        }
    }

//    private static void register(final String hookUrl, final List<String> serverPath) {
//        // TODO call out to ingest.runabout.dev to register ourselves as a target for commands.
//    }

    private static Pair<Class<?>, Method> getReferences(final String identifier) {

        Method method = null;
        if (identifier.contains("#")) {
            method = RunaboutUtils.runaboutStringToMethod(identifier);
        }

        final Class<?> clazz = Optional.ofNullable(method)
                .map(Method::getDeclaringClass)
                .orElseGet(() -> (Class) RunaboutUtils.getClass(identifier)); // TODO confirm this is safe.

        return new Pair<>(clazz, method);
    }

    private static final class Pair<T,U> {

        final T left;
        final U right;

        private Pair(T left, U right) {
            this.left = left;
            this.right = right;
        }
    }
}