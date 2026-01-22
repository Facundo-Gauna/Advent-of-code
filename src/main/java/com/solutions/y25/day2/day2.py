import time

file_path = 'data.txt'
invalids : int = 0

def invalid_sequence(x : str , l : int):
    for r in range(2,l+1):
        if l % r != 0: continue
        valid : bool = True
        curr = 0
        k = l // r
        while curr < l:
            if x[curr:curr+k] != x[:k]:
                valid = False
                break
            curr += k
        if valid: return True

    return False

try:
    with open(file_path, 'r') as f:
        content = f.read()

    start_time = time.perf_counter_ns()

    ranges = content.split(',')

    for r in ranges:
        nums = r.split('-')
        l = int(nums[0])
        r = int(nums[1])
        for i in range(l,r+1):
            s = str(i)
            ln = len(s)
            if invalid_sequence(s,ln):
                print(i)
                invalids += i

    end_time = time.perf_counter_ns()
    elapsed_time = end_time - start_time

    print(invalids)            
    print(f"Execution time: { (elapsed_time * 0.001):.4f} microseconds")
except FileNotFoundError:
    print(f"Error: The file '{file_path}' was not found.")
except Exception as e:
    print(f"An error occurred: {e}")


