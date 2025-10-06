{pkgs ? import <nixpkgs> {}}:
pkgs.mkShell {
  buildInputs = with pkgs; [
      jdk24
      jetbrains.idea-community-bin
  ];
}
