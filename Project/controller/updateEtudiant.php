<?php
/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
include_once '../racine.php';
include_once RACINE . '/service/EtudiantService.php';
$id = $_POST['id'];
$nom = $_POST['nom'];
$prenom = $_POST['prenom'];
$ville = $_POST['ville'];
$sexe = $_POST['sexe'];
$es = new EtudiantService();
$es->update(new Etudiant($id, $nom, $prenom, $ville, $sexe));
header("location:../index.php");
