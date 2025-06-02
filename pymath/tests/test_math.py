import unittest
from pymath.lib.math import is_palindrome

class TestMath(unittest.TestCase):
    def test_is_palindrome(self):
        self.assertTrue(is_palindrome("racecar"))
        self.assertTrue(is_palindrome("A man, a plan, a canal: Panama"))
        self.assertFalse(is_palindrome("hello"))
        self.assertTrue(is_palindrome(""))  # Empty string is a palindrome
        self.assertTrue(is_palindrome("121"))
        self.assertFalse(is_palindrome("123"))

if __name__ == '__main__':
    unittest.main()
