export default [
  {
    files: ["frontend/**/*.js"],
    languageOptions: {
      ecmaVersion: "latest",
      sourceType: "script",
      globals: {
        window: "readonly",
        document: "readonly",
        location: "readonly",
        fetch: "readonly",
        URLSearchParams: "readonly",
        Intl: "readonly",
        Date: "readonly",
        Promise: "readonly",
        console: "readonly",
        confirm: "readonly",
        prompt: "readonly"
      }
    },
    rules: {
      "no-constant-condition": "error",
      "no-dupe-keys": "error",
      "no-unreachable": "error",
      "no-unsafe-finally": "error",
      "no-var": "warn",
      "eqeqeq": ["warn", "always"]
    }
  }
];
