public class Task02Demo {
    public static void main(String[] args) {
        int[] slotsPerFloor = {2, 1, 3};
        String[][] slots = new String[slotsPerFloor.length][];
        for (int floor = 0; floor < slotsPerFloor.length; floor++) {
            slots[floor] = new String[slotsPerFloor[floor]];
        }

        slots[2][1] = "TN01AA1234";
        int minutes = 61;
        long billableHours = (minutes + 59L) / 60L;
        double fee = billableHours * 30.0;
        System.out.println("Floors: " + slots.length);
        System.out.println("Vehicle: " + slots[2][1]);
        System.out.println("Fee for 61 minutes: Rs " + fee);
    }
}
