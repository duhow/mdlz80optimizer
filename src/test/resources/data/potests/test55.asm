; Test case: 

ONE:	equ 1

	ld a,(value)  ; mdl:no-opt-start
	cp ONE*0  ; <-- should be optimized (but will not, since this is a protected block)
	call z,function1
	ld a,2
	ld (value),a
end:
	jp end  ; mdl:no-opt-end


function1:
	ld a, 0	; <-- should be optimized (as it's overwritten by xor a)
	xor a  ; <-- should be optimized (as it's not used afterwards)
	ret


value:
	db 1