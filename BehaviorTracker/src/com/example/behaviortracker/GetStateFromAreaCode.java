package com.example.behaviortracker;

public class GetStateFromAreaCode
{
    /**
     * @param areaCode = the area code
     * @return the state to which that area code belongs
     * 
     * @throws IllegalArgumentException if the argument areaCode is not valid
     */
    public static String getState(int areaCode)
    {
        switch (areaCode)
        {
            case 201:
                return "New Jersey";
            case 202:
                return "Washington,DC";
            case 203:
                return "Connecticut";
            case 205:
                return "Alabama";
            case 206:
                return "Washington";
            case 207:
                return "Maine";
            case 208:
                return "Idaho";
            case 209:
                return "California";
            case 210:
                return "Texas";
            case 212:
                return "New York";
            case 213:
                return "California";
            case 214:
                return "Texas";
            case 215:
                return "Pennsylvania";
            case 216:
                return "Ohio";
            case 217:
                return "llinois";
            case 218:
                return "Minnesota";
            case 219:
                return "Indiana";
            case 224:
                return "llinois";
            case 225:
                return "Louisiana";
            case 228:
                return "Mississippi";
            case 229:
                return "Georgia";
            case 231:
                return "Michigan";
            case 234:
                return "Ohio";
            case 239:
                return "Florida";
            case 240:
                return "Maryland";
            case 248:
                return "Michigan";
            case 251:
                return "Alabama";
            case 252:
                return "North Carolina";
            case 253:
                return "Washington";
            case 254:
                return "Texas";
            case 256:
                return "Alabama";
            case 260:
                return "Indiana";
            case 262:
                return "Wisconsin";
            case 267:
                return "Pennsylvania";
            case 269:
                return "Michigan";
            case 270:
                return "Kentucky";
            case 276:
                return "Virginia";
            case 281:
                return "Texas";
            case 301:
                return "Maryland";
            case 302:
                return "Delaware";
            case 303:
                return "Colorado";
            case 304:
                return "West Virginia";
            case 305:
                return "Florida";
            case 307:
                return "Wyoming";
            case 308:
                return "Nebraska";
            case 309:
                return "llinois";
            case 310:
                return "California";
            case 312:
                return "llinois";
            case 313:
                return "Michigan";
            case 314:
                return "Missouri";
            case 315:
                return "New York";
            case 316:
                return "Kansas";
            case 317:
                return "Indiana";
            case 318:
                return "Louisiana";
            case 319:
                return "Iowa";
            case 320:
                return "Minnesota";
            case 321:
                return "Florida";
            case 323:
                return "California";
            case 325:
                return "Texas";
            case 330:
                return "Ohio";
            case 331:
                return "llinois";
            case 334:
                return "Alabama";
            case 336:
                return "North Carolina";
            case 337:
                return "Louisiana";
            case 339:
                return "Massachusetts";
            case 340:
                return "Virgin Islands";
            case 347:
                return "New York";
            case 351:
                return "Massachusetts";
            case 352:
                return "Florida";
            case 360:
                return "Washington";
            case 361:
                return "Texas";
            case 385:
                return "Utah";
            case 386:
                return "Florida";
            case 401:
                return "Rhode Island";
            case 402:
                return "Nebraska";
            case 404:
                return "Georgia";
            case 405:
                return "Oklahoma";
            case 406:
                return "Montana";
            case 407:
                return "Florida";
            case 408:
                return "California";
            case 409:
                return "Texas";
            case 410:
                return "Maryland";
            case 412:
                return "Pennsylvania";
            case 413:
                return "Massachusetts";
            case 414:
                return "Wisconsin";
            case 415:
                return "California";
            case 417:
                return "Missouri";
            case 419:
                return "Ohio";
            case 423:
                return "Tennessee";
            case 424:
                return "California";
            case 425:
                return "Washington";
            case 430:
                return "Texas";
            case 432:
                return "Texas";
            case 434:
                return "Virginia";
            case 435:
                return "Utah";
            case 440:
                return "Ohio";
            case 442:
                return "California";
            case 443:
                return "Maryland";
            case 458:
                return "Oregon";
            case 469:
                return "Texas";
            case 470:
                return "Georgia";
            case 475:
                return "Connecticut";
            case 478:
                return "Georgia";
            case 479:
                return "Arkansas";
            case 480:
                return "Arizona";
            case 484:
                return "Pennsylvania";
            case 501:
                return "Arkansas";
            case 502:
                return "Kentucky";
            case 503:
                return "Oregon";
            case 504:
                return "Louisiana";
            case 505:
                return "New Mexico";
            case 507:
                return "Minnesota";
            case 508:
                return "Massachusetts";
            case 509:
                return "Washington";
            case 510:
                return "California";
            case 512:
                return "Texas";
            case 513:
                return "Ohio";
            case 515:
                return "Iowa";
            case 516:
                return "New York";
            case 517:
                return "Michigan";
            case 518:
                return "New York";
            case 520:
                return "Arizona";
            case 530:
                return "California";
            case 539:
                return "Oklahoma";
            case 540:
                return "Virginia";
            case 541:
                return "Oregon";
            case 551:
                return "New Jersey";
            case 559:
                return "California";
            case 561:
                return "Florida";
            case 562:
                return "California";
            case 563:
                return "Iowa";
            case 567:
                return "Ohio";
            case 570:
                return "Pennsylvania";
            case 571:
                return "Virginia";
            case 573:
                return "Missouri";
            case 574:
                return "Indiana";
            case 575:
                return "New Mexico";
            case 580:
                return "Oklahoma";
            case 585:
                return "New York";
            case 586:
                return "Michigan";
            case 601:
                return "Mississippi";
            case 602:
                return "Arizona";
            case 603:
                return "New Hampshire";
            case 605:
                return "South Dakota";
            case 606:
                return "Kentucky";
            case 607:
                return "New York";
            case 608:
                return "Wisconsin";
            case 609:
                return "New Jersey";
            case 610:
                return "Pennsylvania";
            case 612:
                return "Minnesota";
            case 614:
                return "Ohio";
            case 615:
                return "Tennessee";
            case 616:
                return "Michigan";
            case 617:
                return "Massachusetts";
            case 618:
                return "llinois";
            case 619:
                return "California";
            case 620:
                return "Kansas";
            case 623:
                return "Arizona";
            case 626:
                return "California";
            case 630:
                return "llinois";
            case 631:
                return "New York";
            case 636:
                return "Missouri";
            case 641:
                return "Iowa";
            case 646:
                return "New York";
            case 650:
                return "California";
            case 651:
                return "Minnesota";
            case 657:
                return "California";
            case 660:
                return "Missouri";
            case 661:
                return "California";
            case 662:
                return "Mississippi";
            case 671:
                return "Guam";
            case 678:
                return "Georgia";
            case 681:
                return "West Virginia";
            case 682:
                return "Texas";
            case 684:
                return "American Samoa";
            case 701:
                return "North Dakota";
            case 702:
                return "Nevada";
            case 703:
                return "Virginia";
            case 704:
                return "North Carolina";
            case 706:
                return "Georgia";
            case 707:
                return "California";
            case 708:
                return "llinois";
            case 712:
                return "Iowa";
            case 713:
                return "Texas";
            case 714:
                return "California";
            case 715:
                return "Wisconsin";
            case 716:
                return "New York";
            case 717:
                return "Pennsylvania";
            case 718:
                return "New York";
            case 719:
                return "Colorado";
            case 720:
                return "Colorado";
            case 724:
                return "Pennsylvania";
            case 727:
                return "Florida";
            case 731:
                return "Tennessee";
            case 732:
                return "New Jersey";
            case 734:
                return "Michigan";
            case 740:
                return "Ohio";
            case 747:
                return "California";
            case 754:
                return "Florida";
            case 757:
                return "Virginia";
            case 760:
                return "California";
            case 762:
                return "Georgia";
            case 763:
                return "Minnesota";
            case 765:
                return "Indiana";
            case 769:
                return "Mississippi";
            case 770:
                return "Georgia";
            case 772:
                return "Florida";
            case 773:
                return "llinois";
            case 774:
                return "Massachusetts";
            case 775:
                return "Nevada";
            case 779:
                return "llinois";
            case 781:
                return "Massachusetts";
            case 785:
                return "Kansas";
            case 786:
                return "Florida";
            case 787:
                return "Puerto Rico";
            case 801:
                return "Utah";
            case 802:
                return "Vermont";
            case 803:
                return "South Carolina";
            case 804:
                return "Virginia";
            case 805:
                return "California";
            case 806:
                return "Texas";
            case 808:
                return "Hawaii";
            case 810:
                return "Michigan";
            case 812:
                return "Indiana";
            case 813:
                return "Florida";
            case 814:
                return "Pennsylvania";
            case 815:
                return "llinois";
            case 816:
                return "Missouri";
            case 817:
                return "Texas";
            case 818:
                return "California";
            case 828:
                return "North Carolina";
            case 830:
                return "Texas";
            case 831:
                return "California";
            case 832:
                return "Texas";
            case 843:
                return "South Carolina";
            case 845:
                return "New York";
            case 847:
                return "llinois";
            case 848:
                return "New Jersey";
            case 850:
                return "Florida";
            case 856:
                return "New Jersey";
            case 857:
                return "Massachusetts";
            case 858:
                return "California";
            case 859:
                return "Kentucky";
            case 860:
                return "Connecticut";
            case 862:
                return "New Jersey";
            case 863:
                return "Florida";
            case 864:
                return "South Carolina";
            case 865:
                return "Tennessee";
            case 870:
                return "Arkansas";
            case 872:
                return "llinois";
            case 878:
                return "Pennsylvania";
            case 901:
                return "Tennessee";
            case 903:
                return "Texas";
            case 904:
                return "Florida";
            case 906:
                return "Michigan";
            case 907:
                return "Alaska";
            case 908:
                return "New Jersey";
            case 909:
                return "California";
            case 910:
                return "North Carolina";
            case 912:
                return "Georgia";
            case 913:
                return "Kansas";
            case 914:
                return "New York";
            case 915:
                return "Texas";
            case 916:
                return "California";
            case 917:
                return "New York";
            case 918:
                return "Oklahoma";
            case 919:
                return "North Carolina";
            case 920:
                return "Wisconsin";
            case 925:
                return "California";
            case 928:
                return "Arizona";
            case 929:
                return "New York";
            case 931:
                return "Tennessee";
            case 936:
                return "Texas";
            case 937:
                return "Ohio";
            case 938:
                return "Alabama";
            case 939:
                return "Puerto Rico";
            case 940:
                return "Texas";
            case 941:
                return "Florida";
            case 947:
                return "Michigan";
            case 949:
                return "California";
            case 951:
                return "California";
            case 952:
                return "Minnesota";
            case 954:
                return "Florida";
            case 956:
                return "Texas";
            case 970:
                return "Colorado";
            case 971:
                return "Oregon";
            case 972:
                return "Texas";
            case 973:
                return "New Jersey";
            case 978:
                return "Massachusetts";
            case 979:
                return "Texas";
            case 980:
                return "North Carolina";
            case 985:
                return "Louisiana";
            case 989:
                return "Michigan";
        }
        return null;
    }
}