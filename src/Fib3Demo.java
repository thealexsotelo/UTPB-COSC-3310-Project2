
public class Fib3Demo {

    private static void fib(int a, int b, int c) {
        // pop rdx
        // pop rcx
        // pop rbx
        // pop rax
        // push rdx

        // cmp rcx, 0
        // jle fn_exit
        if (c <= 0) {
            return;
        }

        // push rax
        // push rbx
        // push rcx
        // push rax
        // call fn_print_number
        System.out.println(a);

        // pop rcx
        // pop rbx
        // pop rax
        // sub rcx, 1
        // add rax, rbx
        // push rbx
        // push rax
        // push rcx
        // call fn_fib
        fib(b, a+b, c-1);
        // fn_exit:
        // ret
    }

    public static void main(String[] args) {
        fib(1, 1, 10);
        // push 1
        // push 1
        // push 10
        // call fn_fib

        // mov rax, 1
        // int 0x80
    }
}