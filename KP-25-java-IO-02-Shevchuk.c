int nth_ap(int a, int d, int n) {
   return (a + (n - 1) * d);
}
int main() {
   int a = 2;
   int d = 1;
   int n = 5;
   return nth_ap(a, d, n);
}