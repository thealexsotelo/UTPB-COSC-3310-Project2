section .data
newline db 0x0a, 0x00
nl_len equ $ - newline
nums db '0123456789'
num0 dq 1
num1 dq 1
num2 dq 1
count dq 2
iter dq 10

section .text
global _start

_start:

init:
mov rdx, 1
mov rcx, nums
add rcx, QWORD [num0]
mov rbx, 1
mov rax, 4
int 0x80

mov rdx, nl_len
mov rcx, newline
mov rbx, 1
mov rax, 4
int 0x80

mov rdx, 1
mov rcx, nums
add rcx, QWORD [num1]
mov rbx, 1
mov rax, 4
int 0x80

mov rdx, nl_len
mov rcx, newline
mov rbx, 1
mov rax, 4
int 0x80

loop_head:
mov r10, QWORD [iter]
cmp QWORD [count], r10
jge loop_exit

loop_body:
mov r10, QWORD [num0]
mov QWORD [num2], r10
mov r10, QWORD [num1]
add QWORD [num0], r10
mov r10, QWORD [num2]
mov QWORD [num1], r10

mov rdx, 1
mov rcx, nums

cmp QWORD [num0], 10
jge decomp

add rcx, QWORD [num0]
mov rbx, 1
mov rax, 4
int 0x80

mov rdx, nl_len
mov rcx, newline
mov rbx, 1
mov rax, 4
int 0x80

loop_tail:
add QWORD [count], 1
jmp loop_head

loop_exit:
mov rax, 1
int 0x80

decomp:
mov r13, 0
mov r12, 0
mov r11, [num0]

decomp_head:
cmp r11, 0
je decomp_exit

decomp_body:
mov rdx, 0
mov rcx, 0
mov rbx, 10
mov rax, r11
div rbx

decomp_tail:
push rdx
add r13, 1
mov r11, rax

jmp decomp_head

decomp_exit:

rev_print_head:
cmp r13, 0
je rev_print_exit

rev_print_body:
mov rdx, 1
mov rcx, nums
pop r11
add rcx, r11
mov rbx, 1
mov rax, 4
int 0x80

rev_print_tail:
sub r13, 1
jmp rev_print_head

rev_print_exit:
mov rdx, nl_len
mov rcx, newline
mov rbx, 1
mov rax, 4
int 0x80
jmp loop_tail
