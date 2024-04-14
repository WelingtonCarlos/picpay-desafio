package com.br.welingtoncarlos.picpaydesafio.transaction;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.welingtoncarlos.picpaydesafio.authorization.AuthorizerService;
import com.br.welingtoncarlos.picpaydesafio.notification.NotificationService;
import com.br.welingtoncarlos.picpaydesafio.wallet.Wallet;
import com.br.welingtoncarlos.picpaydesafio.wallet.WalletRepository;
import com.br.welingtoncarlos.picpaydesafio.wallet.WalletType;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final AuthorizerService authorizerService;
    private final NotificationService notificationService;

    public TransactionService(TransactionRepository transactionRepository, WalletRepository walletRepository,
            AuthorizerService authorizerService, NotificationService notificationService) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.authorizerService = authorizerService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Transaction create(Transaction transaction) {
        // 1 - Validar
        validate(transaction);

        // 2 - Criar Transação
        var newTransaction = transactionRepository.save(transaction);

        // 3 - Debitar da carteira
        var walletPayer = walletRepository.findById((transaction.payer())).get();
        var walletPayee = walletRepository.findById((transaction.payee())).get();
        walletRepository.save(walletPayer.debit(transaction.value()));
        walletRepository.save(walletPayee.credit(transaction.value()));

        // 4 - Chamar serviços externos
        // authorize transaction
        authorizerService.authorize(transaction);

        // Notificação
        notificationService.notify(transaction);

        return newTransaction;
    }

    /**
     * - the payer has a common wallet
     * - the payer has enough balance
     * - the payer is not payee
     * 
     * @param transaction
     */
    private void validate(Transaction transaction) {
        // utilizando uma estratégia de lambda function
        walletRepository.findById((transaction.payee()))
                .map(payee -> walletRepository.findById((transaction.payer()))
                        .map(payer -> isTransactionValid(transaction, payer) ? transaction : null)
                        .orElseThrow(() -> new InvalidTransactionException(
                                "Transação Inválida - %s".formatted(transaction))))
                .orElseThrow(() -> new InvalidTransactionException("Transação Inválida - %s".formatted(transaction)));
    }

    private boolean isTransactionValid(Transaction transaction, Wallet payer) {
        return payer.type() == WalletType.COMMON.getValue()
                && payer.balance().compareTo(transaction.value()) >= 0
                && !payer.id().equals(transaction.payee());
    }

    public List<Transaction> list() {
        return transactionRepository.findAll();
    }
}
