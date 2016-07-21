package org.mengyun.tcctransaction.interceptor;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.mengyun.tcctransaction.Compensable;
import org.mengyun.tcctransaction.InvocationContext;
import org.mengyun.tcctransaction.Participant;
import org.mengyun.tcctransaction.Terminator;
import org.mengyun.tcctransaction.Transaction;
import org.mengyun.tcctransaction.TransactionRepository;
import org.mengyun.tcctransaction.api.TransactionContext;
import org.mengyun.tcctransaction.api.TransactionStatus;
import org.mengyun.tcctransaction.api.TransactionXid;
import org.mengyun.tcctransaction.common.MethodType;
import org.mengyun.tcctransaction.support.TransactionConfigurator;
import org.mengyun.tcctransaction.utils.CompensableMethodUtils;
import org.mengyun.tcctransaction.utils.ReflectionUtils;

/**
 * Created by changmingxie on 11/8/15.
 */
public class ResourceCoordinatorInterceptor {

    private TransactionConfigurator transactionConfigurator;

    public void setTransactionConfigurator(TransactionConfigurator transactionConfigurator) {
        this.transactionConfigurator = transactionConfigurator;
    }

    @SuppressWarnings("incomplete-switch")
	public void interceptTransactionContextMethod(ProceedingJoinPoint pjp) throws Throwable {

        Transaction transaction = transactionConfigurator.getTransactionManager().getCurrentTransaction();

        if (transaction != null && transaction.getStatus().equals(TransactionStatus.TRYING)) {

            TransactionContext transactionContext = CompensableMethodUtils.getTransactionContextFromArgs(pjp.getArgs());

            Compensable compensable = getCompensable(pjp);

            MethodType methodType = CompensableMethodUtils.calculateMethodType(transactionContext, compensable != null ? true : false);

            switch (methodType) {
                case ROOT:
                    generateAndEnlistRootParticipant(pjp);
                    break;
                case CONSUMER:
                    generateAndEnlistConsumerParticipant(pjp);
                    break;
                case PROVIDER:
                    generateAndEnlistProviderParticipant(pjp);
                    break;
            }
        }

        pjp.proceed(pjp.getArgs());
    }

    private Participant generateAndEnlistRootParticipant(ProceedingJoinPoint pjp) {

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        
        Method method = signature.getMethod();
        
        Compensable compensable = getCompensable(pjp);
        
        String confirmMethodName = compensable.confirmMethod();
        
        String cancelMethodName = compensable.cancelMethod();

        Transaction transaction = transactionConfigurator.getTransactionManager().getCurrentTransaction();

        TransactionXid xid = new TransactionXid(transaction.getXid().getGlobalTransactionId());

        @SuppressWarnings("rawtypes")
		Class targetClass = ReflectionUtils.getDeclaringType(pjp.getTarget().getClass(), method.getName(), method.getParameterTypes());

        InvocationContext confirmInvocation = new InvocationContext(targetClass, confirmMethodName, method.getParameterTypes(), pjp.getArgs());
        
        InvocationContext cancelInvocation = new InvocationContext(targetClass, cancelMethodName, method.getParameterTypes(), pjp.getArgs());

        Participant participant = new Participant( xid, new Terminator(confirmInvocation, cancelInvocation) );

        transaction.enlistParticipant(participant);

        TransactionRepository transactionRepository = transactionConfigurator.getTransactionRepository();
        
        transactionRepository.update(transaction);

        return participant;
    }

    private Participant generateAndEnlistConsumerParticipant(ProceedingJoinPoint pjp) {

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();

        Transaction transaction = transactionConfigurator.getTransactionManager().getCurrentTransaction();

        TransactionXid xid = new TransactionXid(transaction.getXid().getGlobalTransactionId());

        int position = CompensableMethodUtils.getTransactionContextParamPosition(((MethodSignature) pjp.getSignature()).getParameterTypes());
        
        /**
         * SY: - Important: 给第一个参数 TransactionContext 赋值
         * method org.mengyun.tcctransaction.sample.dubbo.order.domain.service.PaymentServiceImpl#makePayment 
         * ...
         * capitalTradeOrderService.record(null, buildCapitalTradeOrderDto(order)); // 给远程服务方法第一个参数 null (TransactionContext) 赋值...
         * 原来 AOP 还可以这样干，强行将参数给替换掉... 惊了.... Surprise...
         */
        pjp.getArgs()[position] = new TransactionContext(xid, transaction.getStatus().getId());

        Object[] tryArgs = pjp.getArgs();
        Object[] confirmArgs = new Object[tryArgs.length];
        Object[] cancelArgs = new Object[tryArgs.length];

        System.arraycopy(tryArgs, 0, confirmArgs, 0, tryArgs.length);
        confirmArgs[position] = new TransactionContext(xid, TransactionStatus.CONFIRMING.getId());

        System.arraycopy(tryArgs, 0, cancelArgs, 0, tryArgs.length);
        cancelArgs[position] = new TransactionContext(xid, TransactionStatus.CANCELLING.getId());

        @SuppressWarnings("rawtypes")
		Class targetClass = ReflectionUtils.getDeclaringType(pjp.getTarget().getClass(), method.getName(), method.getParameterTypes());

        InvocationContext confirmInvocation = new InvocationContext(targetClass, method.getName(), method.getParameterTypes(), confirmArgs);
        InvocationContext cancelInvocation = new InvocationContext(targetClass, method.getName(), method.getParameterTypes(), cancelArgs);

        Participant participant = new Participant( xid, new Terminator(confirmInvocation, cancelInvocation) );

        transaction.enlistParticipant(participant);

        TransactionRepository transactionRepository = transactionConfigurator.getTransactionRepository();

        transactionRepository.update(transaction);

        return participant;
    }

    private Participant generateAndEnlistProviderParticipant(ProceedingJoinPoint pjp) {

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        
        Method method = signature.getMethod();

        Compensable compensable = getCompensable(pjp);

        String confirmMethodName = compensable.confirmMethod();
        
        String cancelMethodName = compensable.cancelMethod();

        Transaction transaction = transactionConfigurator.getTransactionManager().getCurrentTransaction();

        TransactionXid xid = new TransactionXid(transaction.getXid().getGlobalTransactionId());

        @SuppressWarnings("rawtypes")
		Class targetClass = ReflectionUtils.getDeclaringType(pjp.getTarget().getClass(), method.getName(), method.getParameterTypes());

        InvocationContext confirmInvocation = new InvocationContext(targetClass, confirmMethodName, method.getParameterTypes(), pjp.getArgs());
        
        InvocationContext cancelInvocation = new InvocationContext(targetClass, cancelMethodName, method.getParameterTypes(), pjp.getArgs());

        Participant participant = new Participant( xid, new Terminator(confirmInvocation, cancelInvocation));

        transaction.enlistParticipant(participant);

        TransactionRepository transactionRepository = transactionConfigurator.getTransactionRepository();
        
        transactionRepository.update(transaction);

        return participant;
    }

    private Compensable getCompensable(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();

        Compensable compensable = method.getAnnotation(Compensable.class);

        if (compensable == null) {
            Method targetMethod = null;
            try {
                targetMethod = pjp.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes());

                if (targetMethod != null) {
                    compensable = targetMethod.getAnnotation(Compensable.class);
                }

            } catch (NoSuchMethodException e) {
                compensable = null;
            }

        }
        return compensable;
    }
}
