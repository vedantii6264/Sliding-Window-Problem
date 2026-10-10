
# Sliding Window Maximum — Java

A Java implementation of the **Sliding Window Maximum** problem using the Deque data structure to efficiently find the maximum element in every contiguous window of size `k`.

## Problem Statement

Given an integer array `nums` and an integer `k`, find the maximum element in every contiguous subarray (sliding window) of size `k`.

### Example

**Input**
```text
nums = [1, 3, -1, -3, 5, 3, 6, 7]
k = 3
```

**Output**
```text
[3, 3, 5, 5, 6, 7]
```

**Explanation**

The maximum element in each window of size 3 is:

| Window | Maximum |
|---|---:|
| `[1, 3, -1]` | 3 |
| `[3, -1, -3]` | 3 |
| `[-1, -3, 5]` | 5 |
| `[-3, 5, 3]` | 5 |
| `[5, 3, 6]` | 6 |
| `[3, 6, 7]` | 7 |

## Approach: Monotonic Deque

A Deque (Double-Ended Queue) allows elements to be added and removed from both ends.

The optimized approach maintains a deque of array indices in decreasing order of their corresponding values.

**Algorithm**
1. Remove indices from the front that are outside the current window.
2. Remove indices from the back whose values are smaller than or equal to the current element.
3. Add the current index to the back of the deque.
4. Once the first complete window is formed, record the value at the front of the deque as the maximum.
5. Repeat until all windows have been processed.

This approach avoids recalculating the maximum for every window.

## Complexity Analysis

- **Time Complexity:** `O(n)` — each index is added to and removed from the deque at most once.
- **Auxiliary Space Complexity:** `O(k)` — the deque stores at most `k` indices.

Here, `n` represents the length of the input array.

## Technologies Used

- Java
- Deque data structure
- Data Structures and Algorithms (DSA)
- Git and GitHub

## Learning Outcomes

- Understanding the sliding window technique.
- Applying a monotonic deque to optimize array problems.
- Analyzing time and space complexity.
- Practicing efficient problem-solving techniques for coding interviews.

## How to Run

**Prerequisites:** Java JDK installed on your system.

1. Clone the repository:

   ```bash
   git clone https://github.com/vedantii6264/Sliding-Window-Problem.git
   ```

2. Navigate to the directory containing `Main.java`.

3. Compile the program:

   ```bash
   javac Main.java
   ```

4. Run the program:

   ```bash
   java Main
   ```

*Note: The example, algorithm, and commands above assume your implementation uses a monotonic deque and a `Main` class. Adjust them if your source code differs.*
