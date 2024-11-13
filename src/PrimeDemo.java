public class PrimeDemo {
    public static void main(String[] args) {
        String nums = "123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899";
        char[] numsChar = nums.toCharArray();

        for (int i = 0; i < 90*2+9; i++) {
            // loop_init:
            // xor r10, r10
            // loop_head:
            // cmp r10, 189
            // jge loop_exit
            String out = "";
            out += numsChar[i];
            if (i >= 9) {
                i += 1;
                out += numsChar[i];
            }
            // mov rcx, nums
            // add rcx, r10
            // mov rdx, 1
            // cmp r10, 9
            // jl print
            // add rdx, 1
            // print:
            // mov rbx, 1
            // mov rax, 4
            // add r10, rdx
            System.out.println(out);
            // int 0x80
            // mov rdx, nl_len
            // mov rcx, newline
            // mov rbx, 1
            // mov rax, 4
            // int 0x80

            // jmp loop_head
            // loop_exit:
        }
    }

}
