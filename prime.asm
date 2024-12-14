section .data
    buffer db 0, 0           ; Buffer for storing a single or two-digit number
    prime_msg db " is prime", 10, 0    ; String for " is prime\n"
    not_prime_msg db " is not prime", 10, 0 ; String for " is not prime\n"

section .bss
    ; No uninitialized data

section .text
    global _start

_start:
    mov rbx, 1              ; Start counter at 1

print_number:
    ; Check if the number is less than 10
    cmp rbx, 10
    jl single_digit         ; If rbx < 10, handle single-digit numbers

double_digit:
    ; Handle two-digit numbers
    mov rax, rbx            ; Copy counter to rax
    mov rcx, 10             ; Divisor

    ; Calculate first digit (tens place)
    xor rdx, rdx            ; Clear remainder
    div rcx                 ; rax = rbx / 10
    add al, '0'             ; Convert quotient to ASCII
    mov byte [buffer], al   ; Store in buffer

    ; Calculate second digit (ones place)
    mov rax, rbx            ; Copy counter back to rax
    xor rdx, rdx            ; Clear remainder
    div rcx                 ; rax = rbx / 10, rdx = remainder (ones place)
    add dl, '0'             ; Convert remainder to ASCII
    mov byte [buffer + 1], dl

    ; Write number to stdout
    mov rax, 1              ; syscall: write
    mov rdi, 1              ; stdout
    lea rsi, [buffer]       ; Address of buffer
    mov rdx, 2              ; Number of bytes to write (2 digits)
    syscall
    jmp check_prime         ; Proceed to check prime status

single_digit:
    ; Handle single-digit numbers
    mov rax, rbx            ; Copy counter to rax
    add al, '0'             ; Convert number to ASCII
    mov byte [buffer], al   ; Store in buffer

    ; Write single digit to stdout
    mov rax, 1              ; syscall: write
    mov rdi, 1              ; stdout
    lea rsi, [buffer]       ; Address of buffer
    mov rdx, 1              ; Number of bytes to write (1 digit)
    syscall

check_prime:
    ; Check if the number is prime
    push rbx                ; Save rbx
    call is_prime
    pop rbx                 ; Restore rbx
    test rax, rax           ; Check if result is zero (not prime)
    jz write_not_prime      ; If zero, write "is not prime"

write_prime:
    ; Write " is prime" message
    mov rax, 1              ; syscall: write
    mov rdi, 1              ; stdout
    lea rsi, [prime_msg]    ; Address of " is prime" message
    mov rdx, 10             ; Length of " is prime" message
    syscall
    jmp next_number

write_not_prime:
    ; Write " is not prime" message
    mov rax, 1              ; syscall: write
    mov rdi, 1              ; stdout
    lea rsi, [not_prime_msg] ; Address of " is not prime" message
    mov rdx, 14             ; Length of " is not prime" message
    syscall

next_number:
    ; Increment counter
    inc rbx                 ; Increment the counter
    cmp rbx, 100            ; Check if we've reached 100
    jl print_number         ; Loop until rbx >= 100

exit:
    ; Exit program
    mov rax, 60             ; syscall: exit
    xor rdi, rdi            ; Exit code 0
    syscall

; Function: is_prime
; Input: rbx (number to check)
; Output: rax (1 if prime, 0 if not prime)
is_prime:
    cmp rbx, 2              ; Numbers < 2 are not prime
    jl not_prime
    cmp rbx, 2              ; 2 is prime
    je is_prime_exit

    ; Initialize divisor
    mov rcx, 2

prime_loop:
    ; Check divisibility: rbx / rcx (no modulus)
    mov rax, rbx            ; Copy number to rax
    xor rdx, rdx            ; Clear remainder
    div rcx                 ; rax = rbx / rcx, rdx = remainder
    cmp rdx, 0              ; Check if remainder is 0
    je not_prime            ; If divisible, it's not prime

    ; Increment divisor and check rcx * rcx <= rbx
    inc rcx
    mov rax, rcx
    mul rcx                 ; rcx * rcx
    cmp rax, rbx            ; Compare square of rcx to rbx
    jbe prime_loop          ; If rcx * rcx <= rbx, continue loop

    ; If no divisors found, it's prime
    mov rax, 1              ; Return 1 (prime)
    ret

not_prime:
    mov rax, 0              ; Return 0 (not prime)
is_prime_exit:
    ret
